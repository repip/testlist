/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testlistbox;

/**
 *
 * @author ale
 */
public class Csc {

    private String csc;
    private String lotto;

    public Csc(String csc, String lotto) {
        this.csc = csc;
        this.lotto = lotto;
    }

    //toString()-method to make it display in a meaningful way in the JList
    public String toString() {
        return csc + " - " + lotto;
    }

}
