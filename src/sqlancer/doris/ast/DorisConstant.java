package sqlancer.doris.ast;

public class DorisConstant implements DorisExpression {

    private DorisConstant() {
    }

    public static class DorisNullConstant extends DorisConstant {

        @Override
        public String toString() {
            return "NULL";
        }

    }

    public static class DorisIntConstant extends DorisConstant {

        private final long value;

        public DorisIntConstant(long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public long getValue() {
            return value;
        }

    }

    public static class DorisDoubleConstant extends DorisConstant {

        private final double value;

        public DorisDoubleConstant(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            if (value == Double.POSITIVE_INFINITY) {
                return "'+Inf'";
            } else if (value == Double.NEGATIVE_INFINITY) {
                return "'-Inf'";
            }
            return String.valueOf(value);
        }

    }

    public static class DorisTextConstant extends DorisConstant {

        private final String value;

        public DorisTextConstant(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
        }

    }

    public static class DorisBitConstant extends DorisConstant {

        private final String value;

        public DorisBitConstant(long value) {
            this.value = Long.toBinaryString(value);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "B'" + value + "'";
        }

    }

    public static class DorisBooleanConstant extends DorisConstant {

        private final boolean value;

        public DorisBooleanConstant(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }

    public static DorisTextConstant createStringConstant(String text) {
        return new DorisTextConstant(text);
    }

    public static DorisDoubleConstant createFloatConstant(double val) {
        return new DorisDoubleConstant(val);
    }

    public static DorisIntConstant createIntConstant(long val) {
        return new DorisIntConstant(val);
    }

    public static DorisNullConstant createNullConstant() {
        return new DorisNullConstant();
    }

    public static DorisConstant createBooleanConstant(boolean val) {
        return new DorisBooleanConstant(val);
    }

}
