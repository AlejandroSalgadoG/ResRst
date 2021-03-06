package reservaciones;

import java.awt.event.*;
import javax.swing.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Time;

public class Sql implements ActionListener{

    String driver = "com.mysql.jdbc.Driver";
    String host = "jdbc:mysql://sql3.freesqldatabase.com:3306/";
    String database = "sql379612";
    String user = "sql379612";
    String pass = "vJ5!tG2*";
    String query;
    String querycliente;
    String queryread;
    Connection con;
    Statement sql;
    PreparedStatement ans;
    ResultSet rs;
    Panel panel;
    JFrame FrameImagen;

    int size;
    int sizeRest;
    int sizeHora;
    int sizeLugar;
    int[] dimension;

    Sql(Panel panel) throws SQLException{
        try {
            this.panel = panel;
            Class.forName(driver);
            con = DriverManager.getConnection(host+database, user, pass);
            sql = con.createStatement();

            readingQuery();
        }catch( SQLException | ClassNotFoundException er ) {
            throw new SQLException();
        }
    }

    public void readingQuery(){
        try{
            rs = sql.executeQuery("SELECT `Nombre Restaurante` FROM Restaurantes");
            while(rs.next()) panel.addItem(1, rs.getString("Nombre Restaurante"));
            sizeRest = panel.getComboBox(1).getItemCount();
        } catch (SQLException ex) {
            panel.error("Error al obtener los nombres de los restaurantes de la base de datos");
        }
    }

    public int getIDRestaurante(){
        int IDRestaurante = 0;
        String rest = panel.getComboBox(1).getSelectedItem().toString();
        if(rest.equals("")){
            panel.error("Por favor seleccione un restaurante");
            panel.enableDisable(0,false);
            panel.enableDisable(1,false);
            panel.enableDisable(2,false);
            panel.enableDisable(3,false);
            return -1;
        }
        try{
            rs = sql.executeQuery("SELECT IDRestaurante "+
                                  "FROM Restaurantes "+
                                  "WHERE `Nombre Restaurante` = " +
                                  "'"+rest+"'");
            if(rs.next()) IDRestaurante = rs.getInt("IDRestaurante");
        }catch(SQLException a){
            panel.error("Error al obtener el id del restaurante");
        }
        return IDRestaurante;
    }

    public  void readTime(int IDrest){
        Time horaI = null;
        Time horaF = null;
        try{
            rs = sql.executeQuery("SELECT `Hora Inicio` "+
                                  "FROM Restaurantes "+
                                  "WHERE IDRestaurante =" + IDrest);
            if(rs.next()) horaI = rs.getTime("Hora Inicio");

            rs = sql.executeQuery("SELECT `Hora Fin` "+
                                  "FROM Restaurantes "+
                                  "WHERE IDRestaurante =" + IDrest);
            if(rs.next()) horaF = rs.getTime("Hora Fin");
            panel.calcular(horaI.getTime(), horaF.getTime());
            sizeHora = panel.getComboBox(2).getItemCount();
        }catch (SQLException ex) {
            panel.error("Error al obtener la hora");
        }
    }

    public void getMesas(int IDrestaurante, Time ti, java.sql.Date fecha){
        try {
            rs = sql.executeQuery("SELECT Mesa "+
                                  "FROM Sitio "+
                                  "WHERE IDRestaurante= "+IDrestaurante+" AND "+
                                  "(SELECT Mesa NOT IN "+
                                  "(SELECT MESA "+
                                  "FROM Reservas "+
                                  "WHERE Fecha = '"+fecha+"' AND Hora='"+ ti +"'))");
            while (rs.next()) panel.addItem(0,rs.getString("Mesa"));
            sizeLugar = panel.getComboBox(0).getItemCount();
        }catch (Exception e) {
            panel.error("Error al conseguir las mesas.");
        }
    }

    public void actionPerformed(ActionEvent eve){
        String id = eve.getActionCommand();

        switch (id) {
            case "RESTAURANTE":
                JComboBox actual = (JComboBox) eve.getSource();
                size = actual.getItemCount();
                if( size == sizeRest){
                    int idrest = getIDRestaurante();
                    if(idrest == -1){
                        panel.enableDisable(0,false);
                        panel.enableDisable(1,false);
                        panel.enableDisable(2,false);
                        panel.enableDisable(3,false);
                        return;
                    }
                    panel.enableDisable(0,false);
                    panel.enableDisable(1,false);
                    panel.enableDisable(2,false);
                    panel.enableDisable(3,true);
                    panel.frame.panelImagen.getImg(getImage(idrest));
                    dimension = panel.frame.panelImagen.getImgSize();
                    if(dimension[0] > 600)
                        panel.frame.setSize(dimension[0],210+dimension[1]);
                    else
                        panel.frame.setSize(600,210+dimension[1]);
                    panel.frame.panelImagen.setImage();
                    sizeRest = actual.getItemCount();
                }
                else sizeRest = size;
                break;
            case "HORA":
                actual = (JComboBox) eve.getSource();
                size = actual.getItemCount();
                if( size == sizeHora){
                    String horaMesa = panel.getComboBox(2).getSelectedItem().toString();
                    if(horaMesa.equals("")){
                        panel.error("Por favor seleccione una hora");
                        panel.enableDisable(0,false);
                        panel.enableDisable(1,false);
                        return;
                    }
                    panel.cleanComboBox(0);
                    int idrestaurante = getIDRestaurante();
                    if(idrestaurante == -1) return;
                    Time ti = Time.valueOf(horaMesa+":00");
                    String date =  panel.getDateFormat();
                    java.sql.Date fecha = java.sql.Date.valueOf(date);
                    getMesas(idrestaurante, ti, fecha);
                    panel.enableDisable(0,true);
                    panel.enableDisable(1,false);

                    sizeHora = actual.getItemCount();
                }
                else sizeHora = size;
                break;
            case "LUGAR":
                actual = (JComboBox) eve.getSource();
                size = actual.getItemCount();
                if( size == sizeLugar ){
                    String l = panel.getComboBox(0).getSelectedItem().toString();
                    if(l.equals("")){
                        panel.error("Por favor seleccione una mesa");
                        panel.enableDisable(1, false);
                        return;
                    }
                    panel.enableDisable(1, true);

                    sizeLugar = actual.getItemCount();
                }
                else sizeLugar = size;
                break;
            case "RESERVAR":
                String nombre;
                String cedula;
                try{
                    nombre = panel.getText(0);
                    cedula = panel.getText(1);
                    if(!nombre.matches("([a-z]|[A-Z]| )+")) throw new Exception();
                    if(!cedula.matches("([0-9])+")) throw new Exception();
                }catch(Exception a){
                    panel.error("Por favor introdusca nombre y cedula validos");
                    return;
                }

                int ced = Integer.parseInt(cedula);

                String lugar = panel.getComboBox(0).getSelectedItem().toString();
                String restaurante = panel.getComboBox(1).getSelectedItem().toString();
                String hora = panel.getComboBox(2).getSelectedItem().toString();

                String date =  panel.getDateFormat();

                Time t = Time.valueOf(hora+":00");

                java.sql.Date fecha = java.sql.Date.valueOf(date);

                try{
                    executeReserva(ced, t, fecha, lugar, restaurante, nombre);
                } catch(SQLException a){
                    panel.error("Error al realizar la reserva");
                }
                break;
        }
    }

    public String getImage(int Idrest){
        String image="";
        try{
            rs = sql.executeQuery("SELECT Imagen FROM Restaurantes WHERE `IDRestaurante` = " + Idrest);
            if(rs.next()) image = rs.getString("Imagen");
        }catch(SQLException a){
            panel.error("Error al obtener la imagen del restaurante");
        }
        return image;
    }

    public void executeReserva(int cedula, Time hora, java.sql.Date fecha,
            String mesa, String restaurante, String nombre) throws SQLException {
        query="INSERT INTO `Reservas`(`Cedula`, `IDRestaurante`, `Fecha`, `Hora`, `Mesa`) VALUES (?,?,?,?,?)";
        querycliente="INSERT INTO `Cliente` (Cedula, Nombre) VALUES (?,?)";

        PreparedStatement reserva = null;
        PreparedStatement cliente = null;
        int idrestaurante = getIDRestaurante();
        try {
            con.setAutoCommit(false);

            reserva = con.prepareStatement(query);
            cliente = con.prepareStatement(querycliente);

            reserva.setInt(1, cedula);
            reserva.setInt(2, idrestaurante);
            reserva.setDate(3, fecha);
            reserva.setTime(4, hora);
            reserva.setString(5, mesa);
            reserva.executeUpdate();

            cliente.setInt(1, cedula);
            cliente.setString(2, nombre);
            cliente.executeUpdate();

            con.commit();

            panel.msg(nombre + ", su reserva se ha realizado"
                + " correctamente\npara el día: " + fecha
                + "\na la(s): " + hora);
        } catch (SQLException ex) {
            if (con != null){
                try{
                    panel.cleanComboBox(0);
                    getMesas(idrestaurante, hora, fecha);
                    con.rollback();
                    panel.error("Error al hacer la transaccion, por favor "+
                                "verifique los datos e intentelo nuevamente");
                }
                catch(SQLException e){
                    panel.error("error al hacer roll back");
                }
            }
        } finally {
            if (reserva != null){
                reserva.close();
            }
            if(cliente != null){
                cliente.close();
            }
            con.setAutoCommit(true);
        }
    }
}
