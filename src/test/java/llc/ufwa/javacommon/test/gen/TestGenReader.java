package llc.ufwa.javacommon.test.gen;

import llc.ufwa.gen.GenDataReader;
import java.util.List;
import java.util.ArrayList;
import llc.ufwa.gen.DataClass;

public class TestGenReader extends GenDataReader {
    @Override
    public List<DataClass> getDataClasses() {
        List<DataClass> l = new ArrayList<DataClass>();
        l.add(new TestGenChunk1());
        return l;
    }
}