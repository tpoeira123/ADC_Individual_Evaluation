package pt.unl.fct.di.adc.webapp.input;

public class InputRequest<T>{

    private T input;

    public InputRequest() {}

    public InputRequest(T input) {
        this.input = input;
    }

    public void setInput(T input) {
        this.input = input;
    }

    public T getInput() {
        return input;
    }
}
