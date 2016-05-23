/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testlistbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * comd sql su database
 *
 *
 * @author amm4
 */
public class MySqlAccess {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public MySqlAccess(String server, String db, String usr, String pwd) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        try {
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + server + "/" + db + "?user=" + usr + "&password=" + pwd);
        } catch (SQLException ex) {
            Logger.getLogger(MySqlAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            statement = connect.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(MySqlAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public CampiDB leggiSigillo(String id) throws SQLException {
        String qta = "";
        CampiDB campi = new CampiDB();
        resultSet = statement
                .executeQuery("select ID, CSC, COD, DES, lotto, QTA from SigilliLt where ID=" + id);
        while (resultSet.next()) {
            campi.setId(resultSet.getInt("ID"));
            campi.setCsc(resultSet.getString("CSC"));
            campi.setCod(resultSet.getString("COD"));
            campi.setDes(resultSet.getString("DES"));
            campi.setLotto(resultSet.getString("lotto"));
            campi.setQta(resultSet.getInt("QTA"));
            campi.setCk(true);
        }
        return campi;
    }

    public CampiDB cscDecode(String id) throws SQLException {
        CampiDB campi = new CampiDB();
        if ("P".equals(id.substring(0, 1))) {
            id = id.substring(1);
            resultSet = statement
                    .executeQuery("SELECT COD, DES, lotto FROM VERSPRD WHERE ID=" + id);
            while (resultSet.next()) {
                campi.setCod(resultSet.getString("COD"));
                campi.setDes(resultSet.getString("DES"));
                campi.setLotto(resultSet.getString("lotto"));
                campi.setCsc("P" + id);
                campi.setCk(true);
            }
        } else {
            resultSet = statement
                    .executeQuery("SELECT CDARY9, DSARY9, CDLTY9 FROM YYCSC99F WHERE NRSEY9=" + id);
            while (resultSet.next()) {
                campi.setCod(resultSet.getString("CDARY9"));
                campi.setDes(resultSet.getString("DSARY9"));
                campi.setLotto(resultSet.getString("CDLTY9"));
                campi.setCsc(id);
                campi.setCk(true);
            }
            if (!campi.isCk()) {
                resultSet = statement
                        .executeQuery("SELECT ID, COD, DES, lotto FROM VERSPRD WHERE ORDINE='" + id + "'");
                while (resultSet.next()) {
                    campi.setCsc("P" + Integer.toString(resultSet.getInt("ID")));
                    campi.setCod(resultSet.getString("COD"));
                    campi.setDes(resultSet.getString("DES"));
                    campi.setLotto(resultSet.getString("lotto"));
                    campi.setCk(true);
                }

            }

        }
        return campi;
    }

    public void creaSigillo(CampiDB campi, int qta) throws SQLException {
        preparedStatement = connect
                .prepareStatement("INSERT INTO SIGILLI (CSC, QTA) VALUES (?,?)");
        preparedStatement.setString(1, campi.getCsc());
        preparedStatement.setInt(2, qta);
        preparedStatement.executeUpdate();
        resultSet = statement
                .executeQuery("SELECT ID FROM SIGILLI WHERE ID=LAST_INSERT_ID() AND CSC='" + campi.getCsc() + "'");
        campi.setCk(false);
        while (resultSet.next()) {
            campi.setId(resultSet.getInt("ID"));
            campi.setCk(true);
        }
    }

    /**
     *
     * @param campi
     * @param qta
     * @throws SQLException
     *
     * New db table CREATE TABLE IF NOT EXISTS `SIGILLIMOV` ( `ID` int(11) NOT
     * NULL, `SIG` int(10) NOT NULL, `COD` varchar(20) NOT NULL, `DES`
     * varchar(40) NOT NULL, `LOT` varchar(30) NOT NULL, `OLDQTA` int(11) NOT
     * NULL, `QTAPRE` int(11) NOT NULL, `NEWQTA` int(11) NOT NULL, `DT`
     * timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;
     *
     */
    public void updtSigillo(CampiDB campi, int qta) throws SQLException {
        preparedStatement = connect
                .prepareStatement("UPDATE SIGILLI SET QTA=? WHERE ID=?");
        preparedStatement.setInt(1, campi.getQta() - qta);
        preparedStatement.setInt(2, campi.getId());
        preparedStatement.executeUpdate();
        preparedStatement = connect
                .prepareStatement("INSERT INTO SIGILLIMOV (SIG, COD, DES, LOT, OLDQTA, QTAPRE, NEWQTA) VALUES(?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setInt(1, campi.getId());
        preparedStatement.setString(2, campi.getCod());
        preparedStatement.setString(3, campi.getDes());
        preparedStatement.setString(4, campi.getLotto());
        preparedStatement.setInt(5, campi.getQta());
        preparedStatement.setInt(6, qta);
        preparedStatement.setInt(7, campi.getQta() - qta);
        preparedStatement.executeUpdate();
        //System.out.println(preparedStatement.toString());
    }

    public DefaultListModel csc(String src) throws Exception {
        resultSet = statement
                .executeQuery("SELECT NRSEY9, CDLTY9 FROM YYCSC99F WHERE CDLTY9 LIKE '%" + src + "%' ORDER BY CDLTY9");

        DefaultListModel listModel = new DefaultListModel();
        while (resultSet.next()) {
            String csc = resultSet.getString("NRSEY9");
            String lotto = resultSet.getString("CDLTY9");
            Csc dati = new Csc(csc, lotto);
            listModel.addElement(dati);
        }
        return listModel;
    }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

}
