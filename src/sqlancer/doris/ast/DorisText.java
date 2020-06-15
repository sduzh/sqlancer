package sqlancer.doris.ast;

public class DorisText implements DorisExpression {

    private final String text;

    public DorisText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
