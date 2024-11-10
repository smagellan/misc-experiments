package smagellan.test.jnr;

import jnr.constants.platform.TCP;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.SocketImpl;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JnrTest {
    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        System.err.println(TCP.TCP_USER_TIMEOUT.defined());
        POSIX p = POSIXFactory.getPOSIX();
        int tcpTimeout = 20000;
        System.err.println("setsockopt:");
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(tcpTimeout);

        try (ServerSocket socket = new ServerSocket(0)) {
            Set<Method> methods = Arrays.stream(socket.getClass()
                            .getDeclaredMethods())
                    .filter(m -> m.getName().contains("getImpl"))
                    .collect(Collectors.toSet());
            System.err.println(methods);
            Method getFD = socket.getClass().getMethod("getImpl");
            getFD.setAccessible(true);
            SocketImpl socketImpl = (SocketImpl)getFD.invoke(socket);
            System.err.println(socketImpl);


            //System.err.println(p.libc().setsockopt(fd, IPProto.IPPROTO_TCP.intValue(), TCP.TCP_USER_TIMEOUT.intValue(), bb, Integer.BYTES));
        }
    }
}
