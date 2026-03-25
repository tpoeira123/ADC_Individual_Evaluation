package pt.unl.fct.di.adc.webapp.input;

import pt.unl.fct.di.adc.webapp.util.AuthToken;

public class InputRequest<T>{

    private T input;
    private AuthToken token;

    public InputRequest() {}

    public InputRequest(T input, AuthToken token) {
        this.input = input;
        this.token = token;
    }

    public void setInput(T input) {
        this.input = input;
    }
    public T getInput() {
        return input;
    }

    public void setToken(AuthToken token) {this.token = token;}
    public AuthToken getToken() {return token;}
}
