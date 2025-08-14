package model;

public class ObjetoPerdido {
    private int id;
    private String nombre;
    private String marca;
    private String color;
    private String descripcion;
    private String estado;
    private String imagen;

    public ObjetoPerdido(int id, String nombre, String marca, String color, String descripcion, String estado, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.color = color;
        this.descripcion = descripcion;
        this.estado = estado;
        this.imagen = imagen;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public String getColor() { return color; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public String getImagen() { return imagen; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setColor(String color) { this.color = color; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    @Override
    public String toString() {
        return "ObjetoPerdido{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", marca='" + marca + '\'' +
                ", color='" + color + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", estado='" + estado + '\'' +
                ", imagen='" + imagen + '\'' +
                '}';
    }
}