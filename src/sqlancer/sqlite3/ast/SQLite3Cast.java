package sqlancer.sqlite3.ast;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;
import java.util.regex.Pattern;

import sqlancer.sqlite3.schema.SQLite3DataType;

public final class SQLite3Cast {

    private static final double MAX_INT_FOR_WHICH_CONVERSION_TO_INT_IS_TRIED = Math.pow(2, 51 - 1) - 1;
    private static final double MIN_INT_FOR_WHICH_CONVERSION_TO_INT_IS_TRIED = -Math.pow(2, 51 - 1);

    private static final byte FILE_SEPARATOR = 0x1c;
    private static final byte GROUP_SEPARATOR = 0x1d;
    private static final byte RECORD_SEPARATOR = 0x1e;
    private static final byte UNIT_SEPARATOR = 0x1f;
    private static final byte SYNCHRONOUS_IDLE = 0x16;

    static Connection castDatabase;

    private SQLite3Cast() {
    }

    public static Optional<Boolean> isTrue(SQLite3Constant value) {
        SQLite3Constant numericValue;
        if (value.getDataType() == SQLite3DataType.NULL) {
            return Optional.empty();
        }
        if (value.getDataType() == SQLite3DataType.TEXT || value.getDataType() == SQLite3DataType.BINARY) {
            numericValue = castToNumeric(value);
        } else {
            numericValue = value;
        }
        assert numericValue.getDataType() != SQLite3DataType.TEXT : numericValue + "should have been converted";
        switch (numericValue.getDataType()) {
        case INT:
            return Optional.of(numericValue.asInt() != 0);
        case REAL:
            double doubleVal = numericValue.asDouble();
            return Optional.of(doubleVal != 0 && !Double.isNaN(doubleVal));
        default:
            throw new AssertionError(numericValue);
        }
    }

    // SELECT CAST('-1.370998801E9' AS INTEGER) == -1
    public static SQLite3Constant castToInt(SQLite3Constant cons) {
        if (cons.getDataType() == SQLite3DataType.BINARY) {
            String text = new String(cons.asBinary());
            cons = SQLite3Constant.createTextConstant(text);
        }
        switch (cons.getDataType()) {
        case NULL:
            return SQLite3Constant.createNullConstant();
        case INT:
            return cons;
        case REAL:
            return SQLite3Constant.createIntConstant((long) cons.asDouble());
        case TEXT:
            String asString = cons.asString();
            while (startsWithWhitespace(asString)) {
                asString = asString.substring(1);
            }
            if (!asString.isEmpty() && unprintAbleCharThatLetsBecomeNumberZero(asString)) {
                return SQLite3Constant.createIntConstant(0);
            }
            for (int i = asString.length(); i >= 0; i--) {
                try {
                    String substring = asString.substring(0, i);
                    Pattern p = Pattern.compile("[+-]?\\d\\d*");
                    if (p.matcher(substring).matches()) {
                        BigDecimal bg = new BigDecimal(substring);
                        long result;
                        try {
                            result = bg.longValueExact();
                        } catch (ArithmeticException e) {
                            if (substring.startsWith("-")) {
                                result = Long.MIN_VALUE;
                            } else {
                                result = Long.MAX_VALUE;
                            }
                        }
                        return SQLite3Constant.createIntConstant(result);
                    }
                } catch (Exception e) {

                }
            }
            return SQLite3Constant.createIntConstant(0);
        default:
            throw new AssertionError();
        }

    }

    public static SQLite3Constant castToReal(SQLite3Constant cons) {
        SQLite3Constant numericValue = castToNumeric(cons);
        if (numericValue.getDataType() == SQLite3DataType.INT) {
            return SQLite3Constant.createRealConstant(numericValue.asInt());
        } else {
            return numericValue;
        }
    }

    public static SQLite3Constant castToNumericNoNumAsRealZero(SQLite3Constant value) {
        return convertInternal(value, false, true, true);
    }

    public static SQLite3Constant castToNumericFromNumOperand(SQLite3Constant value) {
        return convertInternal(value, false, false, false);
    }

    /**
     * Applies numeric affinity to a value.
     */
    public static SQLite3Constant castToNumeric(SQLite3Constant value) {
        return convertInternal(value, true, false, false);
    }

    private static SQLite3Constant convertInternal(SQLite3Constant value, boolean convertRealToInt,
            boolean noNumIsRealZero, boolean convertIntToReal) throws AssertionError {
        if (value.getDataType() == SQLite3DataType.BINARY) {
            String text = new String(value.asBinary());
            value = SQLite3Constant.createTextConstant(text);
        }
        switch (value.getDataType()) {
        case NULL:
            return SQLite3Constant.createNullConstant();
        case INT:
        case REAL:
            return value;
        case TEXT:
            String asString = value.asString();
            while (startsWithWhitespace(asString)) {
                asString = asString.substring(1);
            }
            if (!asString.isEmpty() && unprintAbleCharThatLetsBecomeNumberZero(asString)) {
                return SQLite3Constant.createIntConstant(0);
            }
            if (asString.toLowerCase().startsWith("-infinity") || asString.toLowerCase().startsWith("infinity")
                    || asString.startsWith("NaN")) {
                return SQLite3Constant.createIntConstant(0);
            }
            for (int i = asString.length(); i >= 0; i--) {
                try {
                    String substring = asString.substring(0, i);
                    double d = Double.valueOf(substring);
                    BigDecimal first = new BigDecimal(substring);
                    long longValue = first.longValue();
                    BigDecimal second = BigDecimal.valueOf(longValue);
                    boolean isWithinConvertibleRange = longValue >= MIN_INT_FOR_WHICH_CONVERSION_TO_INT_IS_TRIED
                            && longValue <= MAX_INT_FOR_WHICH_CONVERSION_TO_INT_IS_TRIED && convertRealToInt;
                    boolean isFloatingPointNumber = substring.contains(".") || substring.toUpperCase().contains("E");
                    boolean doubleShouldBeConvertedToInt = isFloatingPointNumber && first.compareTo(second) == 0
                            && isWithinConvertibleRange;
                    boolean isInteger = !isFloatingPointNumber && first.compareTo(second) == 0;
                    if (doubleShouldBeConvertedToInt || isInteger && !convertIntToReal) {
                        // see https://www.sqlite.org/src/tktview/afdc5a29dc
                        return SQLite3Constant.createIntConstant(first.longValue());
                    } else {
                        return SQLite3Constant.createRealConstant(d);
                    }
                } catch (Exception e) {
                }
            }
            if (noNumIsRealZero) {
                return SQLite3Constant.createRealConstant(0.0);
            } else {
                return SQLite3Constant.createIntConstant(0);
            }
        default:
            throw new AssertionError(value);
        }
    }

    private static boolean startsWithWhitespace(String asString) {
        if (asString.isEmpty()) {
            return false;
        }
        char c = asString.charAt(0);
        switch (c) {
        case ' ':
        case '\t':
        case 0x0b:
        case '\f':
        case '\n':
        case '\r':
            return true;
        default:
            return false;
        }
    }

    private static boolean unprintAbleCharThatLetsBecomeNumberZero(String s) {
        // non-printable characters are ignored by Double.valueOf
        for (int i = 0; i < s.length(); i++) {
            char charAt = s.charAt(i);
            if (!Character.isISOControl(charAt) && !Character.isWhitespace(charAt)) {
                return false;
            }
            switch (charAt) {
            case GROUP_SEPARATOR:
            case FILE_SEPARATOR:
            case RECORD_SEPARATOR:
            case UNIT_SEPARATOR:
            case SYNCHRONOUS_IDLE:
                return true;
            default:
                // fall through
            }

            if (Character.isWhitespace(charAt)) {
                continue;
            } else {
                return true;
            }
        }
        return false;
    }

    public static SQLite3Constant castToText(SQLite3Constant cons) {
        if (cons.getDataType() == SQLite3DataType.TEXT) {
            return cons;
        }
        if (cons.getDataType() == SQLite3DataType.NULL) {
            return cons;
        }
        if (cons.getDataType() == SQLite3DataType.REAL) {
            if (cons.asDouble() == Double.POSITIVE_INFINITY) {
                return SQLite3Constant.createTextConstant("Inf");
            } else if (cons.asDouble() == Double.NEGATIVE_INFINITY) {
                return SQLite3Constant.createTextConstant("-Inf");
            }
            return castRealToText(cons);
            // if (true) {
            // throw new IgnoreMeException();
            // }
            // NumberFormat fmt = NumberFormat.getInstance();
            // fmt.setGroupingUsed(false);
            // fmt.setMaximumIntegerDigits(10);
            // fmt.setMinimumFractionDigits(1);
            // fmt.setRoundingMode(RoundingMode.UNNECESSARY);
            // int digits;
            // if (cons.asDouble() < 0) {
            // digits = 15;
            // } else {
            // digits = 15;
            // }
            // fmt.setMaximumFractionDigits(digits);
            // try {
            // String s = fmt.format(cons.asDouble());
            // if (s.contentEquals("")) {
            // throw new IgnoreMeException();
            // }
            // return SQLite3Constant.createTextConstant(s);
            // } catch (Exception e) {
            // throw new IgnoreMeException();
            // }
        }
        if (cons.getDataType() == SQLite3DataType.INT) {
            return SQLite3Constant.createTextConstant(String.valueOf(cons.asInt()));
        }
        // if (cons.getDataType() == SQLite3DataType.BINARY) {
        // try {
        // return SQLite3Constant.createTextConstant(new String(cons.asBinary(), "UTF-8").replaceAll("\\p{C}", ""));
        // } catch (UnsupportedEncodingException e) {
        // throw new AssertionError(e);
        // }
        // }
        return null;
        // throw new AssertionError();
    }

    private static synchronized SQLite3Constant castRealToText(SQLite3Constant cons) throws AssertionError {
        try (Statement s = castDatabase.createStatement()) {
            String castResult = s.executeQuery("SELECT CAST(" + cons.asDouble() + " AS TEXT)").getString(1);
            return SQLite3Constant.createTextConstant(castResult);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static SQLite3Constant asBoolean(SQLite3Constant val) {
        Optional<Boolean> boolVal = isTrue(val);
        if (boolVal.isPresent()) {
            return SQLite3Constant.createBoolean(boolVal.get());
        } else {
            return SQLite3Constant.createNullConstant();
        }
    }

    public static SQLite3Constant castToBlob(SQLite3Constant cons) {
        if (cons.isNull()) {
            return cons;
        } else {
            SQLite3Constant stringVal = SQLite3Cast.castToText(cons);
            if (stringVal == null) {
                return null;
            } else {
                return SQLite3Constant.createBinaryConstant(stringVal.asString().getBytes());
            }
        }
    }

}
