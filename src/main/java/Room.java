public class Room {
    private int Id;
    private String name;

    public Room(int ID, String NAME){
        this.Id = ID;
        this.name = NAME;
    }

    public String toString(){
        return this.Id+ " \t "+ this.name;
    }

    public int getID(){
        return Id;
    }

    public boolean Equals(Room other){
        return this.Id == other.Id;
    }
}
