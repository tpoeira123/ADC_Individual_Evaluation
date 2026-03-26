package pt.unl.fct.di.adc.webapp.util;

public class AttributesData {

    private String phone;
    private String address;

    public AttributesData() {}

    public AttributesData(String phone, String address) {
        this.phone = phone;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
