package org.writer;

/**
 * Factory writer producer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FactoryProducer {
    public static AbstractFactory getFactory(String choice) throws Exception {

        if(choice.equalsIgnoreCase("NODE")){
            return new WriterNodeFactory();

        } else if(choice.equalsIgnoreCase("FILE")){
            return new WriterFileFactory();
        }

        throw new Exception(String.format("%s is not allowed to produce a writer factory", choice));
    }
}
