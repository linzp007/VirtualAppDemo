package com.github.tamir7.contacts;

public class ContactData implements IContact {
    private String displayName;
    private String number;
    private long id;

    public ContactData(String displayName, String number) {
        this.displayName = displayName;
        this.number = number;
    }

    public ContactData() {

    }

    public ContactData(Contact contact) {
        this.displayName = contact.getDisplayName();
        if (contact.getPhoneNumbers() != null && contact.getPhoneNumbers().size() > 0) {
            this.number = contact.getPhoneNumbers().get(0).getNumber();
        } else {
            this.number = null;
        }
        this.id = contact.getId();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getPhoneNumber() {
        return number;
    }

    @Override
    public long getContactId() {
        return id;
    }
}
