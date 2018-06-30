package com.qskx.mq;

import org.junit.Test;

/**
 * @author 111111
 * @date 2018-06-30 10:25
 */
public class GenericMethod {

    @Test
    public void test01(){
       Car car1 = Car.buy(200000.0, new Car("red", 2000000.0));
        System.out.println("**************** " + car1.toString());
    }
}
class Car{

    private String coler;
    private Double money;
    Car(String color, Double money){
        this.coler = color;
        this.money = money;
    }
    public static <T> T buy(Double money, T t){
        if (t instanceof Car){
            ((Car) t).setMoney(1000000.0);
        }
        return t;
    }

    public String getColer() {
        return coler;
    }

    public void setColer(String coler) {
        this.coler = coler;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Car{" +
                "coler='" + coler + '\'' +
                ", money=" + money +
                '}';
    }
}

