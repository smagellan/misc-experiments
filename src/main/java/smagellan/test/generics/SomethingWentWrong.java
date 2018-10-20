package smagellan.test.generics;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

public class SomethingWentWrong {
    public static final int DATASET_COUNT = 1000 * 1000;

    public static void main(String[] args) {
        List<TypeReference> typeReferences = new ArrayList<>(DATASET_COUNT);
        for (int i = 0; i < DATASET_COUNT; ++i) {
            ClientDataSet ds = receiveDataSet();
            TypeReference typeRef = ClientDataSet.getDigestFor(ds);
            typeReferences.add(typeRef);
        }


        System.err.println("got " + typeReferences.size() + " dataset digests");
    }

    public static ClientDataSet receiveDataSet() {
        return new ClientDataSet();
    }
}

class ClientDataSet {
    private final byte[] clientBytes;

    public ClientDataSet() {
        this.clientBytes = new byte[100 * 1000];
    }



    public TypeReference getDigest() {
        System.err.println("analyzing " + clientBytes.length + " bytes");
        return new TypeReference<List<ClientDataSet>>() {
            {
                System.err.println("bytes:" + clientBytes.length);
            }
        };
    }

    public static TypeReference getDigestFor(ClientDataSet ds) {
        //System.err.println("analyzing " + ds.clientBytes.length + " bytes");
        return new TypeReference<List<ClientDataSet>>() {
            {
                System.err.println("bytes:" + ds.clientBytes.length);
            }
        };
    }
}
