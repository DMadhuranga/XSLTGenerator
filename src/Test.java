import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Test {
    public static void main(String[] args){
        String xslt= "<users><user><bio><name>Danushka</name><age>15</age></bio><education><school><name>Royal collegeColombo 07</name></school></education></user><user><bio><name>Madhuranga</name><age>22</age></bio><education><school><name>Ananada collegeColombo 10</name></school></education></user></users>";
        String current = "<users><user><bio><name>Danushka</name><age>15.0</age></bio><education><school><name>Royal collegeColombo 07</name></school></education></user><user><bio><name>Madhuranga</name><age>22.0</age></bio><education><school><name>Ananada collegeColombo 10</name></school></education></user></users>";
        System.out.println(xslt.length());
        System.out.println(current.length());
        for(int i=0;i<xslt.length();i++){
            if(xslt.charAt(i)!=current.charAt(i)){
                System.out.println(i);
                System.out.println(xslt.charAt(i));
                System.out.println(current.charAt(i));
                break;
            }
        }
    }
}
