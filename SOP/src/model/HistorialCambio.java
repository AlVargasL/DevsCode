package model;

import javafx.beans.property.*;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class HistorialCambio { // no se mostrará
    private String nombre;
    private String marca;
    private String color;
    private String accion;
    private String fecha;

    public HistorialCambio( String nombre, String marca, String color, String accion, String fecha) {
        this.nombre = nombre;
        this.marca = marca;
        this.color = color;
        this.accion = accion;
        this.fecha = fecha;
    }

     // usado internamente si se necesita
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public String getColor() { return color; }
    public String getAccion() { return accion; }
    public String getFecha() { return fecha; }
}
