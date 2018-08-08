import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Test {
    public static void main(String[] args){
        System.out.println(StringUtils.substring("student/bio/name",StringUtils.lastIndexOf("student/bio/name","/")+1));


        String[] array = "student/bio/name".split("/");
        ArrayUtils.remove(array,0);
        System.out.println(array);
    }
}
