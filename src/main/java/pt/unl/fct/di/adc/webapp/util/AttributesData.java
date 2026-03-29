package pt.unl.fct.di.adc.webapp.util;


/**
 * Data Transfer Object for the nested "attributes" JSON block.
 * Used exclusively during the Modify Account operation (Op5) to parse
 * the specific fields a user is attempting to update.
 */
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
