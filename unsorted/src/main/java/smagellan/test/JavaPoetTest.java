package smagellan.test;

import java.io.IOException;

import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class JavaPoetTest {
    public static void main(String[] args) throws IOException {
        genEnum();
        new GuavaModule();
    }

    private static void genEnum() throws IOException {
        TypeSpec planetTypeSpec =
                TypeSpec.enumBuilder("Planet")
                        .addModifiers(Modifier.PUBLIC)
                        .addEnumConstant("MERCURY")
                        .addEnumConstant("VENUS")
                        .build();
        JavaFile javaFile = JavaFile.builder("com.example.helloworld", planetTypeSpec)
                .build();

        javaFile.writeTo(System.out);
    }

    private static void genHelloWorld() throws IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();

        javaFile.writeTo(System.out);
    }
}
