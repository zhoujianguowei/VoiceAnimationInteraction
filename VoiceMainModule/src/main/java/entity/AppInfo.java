package entity;
import android.graphics.drawable.Drawable;
public class AppInfo
{

    private String packageName;
    private String label;
    private Drawable icon;
    public String getPackageName()
    {
        return packageName;
    }
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
    public String getLabel()
    {
        return label;
    }
    public void setLabel(String label)
    {
        this.label = label;
    }
    public Drawable getIcon()
    {
        return icon;
    }
    public void setIcon(Drawable icon)
    {
        this.icon = icon;
    }
    @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        // return super.toString();
        return "包名：" + packageName + " 标签：" + label;
    }
}
