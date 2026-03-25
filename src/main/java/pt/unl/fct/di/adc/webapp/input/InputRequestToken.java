package pt.unl.fct.di.adc.webapp.input;

public class InputRequestToken <T>{

    private T input;
    private String token;

    public InputRequestToken() {}

    public InputRequestToken(T input) {
        this.input = input;
    }

    public InputRequestToken(T input, String token) {
        this.input = input;
        this.token = token;
    }

    public void setInput(T input) {
        this.input = input;
    }

    public T getInput() {
        return input;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
}
