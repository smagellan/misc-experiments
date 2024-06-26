package smagellan.test;

import java.io.IOException;
import org.apache.carbondata.common.exceptions.sql.InvalidLoadOptionException;
import org.apache.carbondata.core.constants.CarbonCommonConstants;
import org.apache.carbondata.core.metadata.datatype.DataTypes;
import org.apache.carbondata.core.util.CarbonProperties;
import org.apache.carbondata.sdk.file.CarbonWriter;
import org.apache.carbondata.sdk.file.CarbonWriterBuilder;
import org.apache.carbondata.core.metadata.datatype.Field;
import org.apache.carbondata.sdk.file.Schema;

//https://carbondata.apache.org/sdk-guide.html
public class CarbondataTestSdk {
    // pass true or false while executing the main to use offheap memory or not
    public static void main(String[] args) throws IOException, InvalidLoadOptionException {
        if (args.length > 0 && args[0] != null) {
            testSdkWriter(args[0]);
        } else {
            testSdkWriter("true");
        }
    }

    public static void testSdkWriter(String enableOffheap) throws IOException, InvalidLoadOptionException {
        String path = "/tmp/testCSVSdkWriter";

        Field[] fields = new Field[2];
        fields[0] = new Field("name", DataTypes.STRING);
        fields[1] = new Field("age", DataTypes.INT);

        Schema schema = new Schema(fields);

        CarbonProperties.getInstance()
                .addProperty(CarbonCommonConstants.ENABLE_OFFHEAP_SORT, enableOffheap)
                .addProperty(CarbonCommonConstants.COMPRESSOR, "zstd");

        CarbonWriterBuilder builder = CarbonWriter.builder()
                .outputPath(path)
                .withCsvInput(schema)
                .writtenBy("SDK");

        CarbonWriter writer = builder.build();

        int rows = 5;
        for (int i = 0; i < rows; i++) {
            writer.write(new String[] { "robot" + (i % 10), String.valueOf(i) });
        }
        writer.close();
    }
}
