import com.qc.printers.common.signin.domain.entity.SigninWay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class test {
    public static void main(String args[]){
        HashMap<String,String> s= new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        String k = String.valueOf(currentDate.getDayOfWeek());
        int value = currentDate.getDayOfWeek().getValue();
        System.out.println(value);
    }
}
