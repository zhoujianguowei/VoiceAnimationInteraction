package entity;
/**
 * Created by Administrator on 2015/6/3.
 */

/**
 * 只有手机号和联系号码，并且是一对一关系
 */
public class SimpleContact
{

    String name;
    String phone;
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getPhone()
    {
        return phone;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
}

