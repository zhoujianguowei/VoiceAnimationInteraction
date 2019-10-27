package entity;
import java.util.HashSet;
import java.util.Iterator;
public class Contact
{

    private String name;
    private HashSet<String> phones;
    @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        // return super.toString();
        if (phones.size() == 0)
            return null;
        Iterator<String> iterator = phones.iterator();
        StringBuilder builder = new StringBuilder("姓名：" + name + "    电话号码：");
        while (iterator.hasNext())
        {
            builder.append(iterator.next());
        }
        return builder.toString();
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public HashSet<String> getPhones()
    {
        return phones;
    }
    public void setPhones(HashSet<String> phones)
    {
        this.phones = phones;
    }
}
