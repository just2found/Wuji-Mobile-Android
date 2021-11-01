package net.sdvn.nascommon.model.contacts;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * @author xiaobo.cui 2014年11月24日 下午5:36:29
 */
@Keep
public class Contact {
    public Contact() {

    }

    public Contact(String name, @Nullable String number, String sortKey) {
        this.name = name;
        this.sortKey = sortKey;
        this.number = number;
        if (number != null) {
            String regex = "-|\\s";
            int index = number.indexOf(" ");
            if (number.startsWith("+") && index > 0) {
                try {
                    countryCode = (number.substring(1, index));
                    simpleNumber = number.substring(index).replaceAll(regex, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    this.simpleNumber = number.replaceAll(regex, "");
                }
            } else
                this.simpleNumber = number.replaceAll(regex, "");
        }
    }

    public String name;
    @Nullable
    public String number;
    public String simpleNumber;
    public String sortKey;
    public String countryCode;


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((sortKey == null) ? 0 : sortKey.hashCode());
        result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Contact other = (Contact) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (number == null) {
            if (other.number != null)
                return false;
        } else if (!number.equals(other.number))
            return false;

        if (sortKey == null) {
            return other.sortKey == null;
        } else if (!sortKey.equals(other.sortKey))
            return false;
        return Objects.equals(countryCode, other.countryCode);

    }

}
