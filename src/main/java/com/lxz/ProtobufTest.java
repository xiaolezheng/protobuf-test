package com.lxz;

import com.lxz.protobuf.AddressBookProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * protobuf 序列化测试
 *
 * Created by xiaolezheng on 16/4/22.
 */
public class ProtobufTest {
    private static final Logger logger = LoggerFactory.getLogger(ProtobufTest.class);

    public static void main(String[] args) throws Exception{
        AddressBookProtos.Person john =
                AddressBookProtos.Person.newBuilder()
                        .setId(1234)
                        .setName("John Doe")
                        .setEmail("jdoe@example.com")
                        .addPhone(
                                AddressBookProtos.Person.PhoneNumber.newBuilder()
                                        .setNumber("555-4321")
                                        .setType(AddressBookProtos.Person.PhoneType.HOME))
                        .build();


        logger.info("person: {}", john);

        byte[] bytes = john.toByteArray();

        logger.info("bytes size: {}", bytes.length);

        AddressBookProtos.Person johnCp = AddressBookProtos.Person.parseFrom(bytes);

        logger.info("person cp: {}", johnCp);

        byte[] bytesCp = johnCp.toByteArray();

        logger.info("bytes cp size: {}", bytesCp.length);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(john);

        logger.info("java serializable byte  size: {}", byteArrayOutputStream.toByteArray().length);



    }
}
