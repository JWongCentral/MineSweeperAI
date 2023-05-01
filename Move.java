package MineSweeper;

/*
Object used to store next possible move
makes it easier to store results of AI
 */
public class Move {

    public int row,column;
    public boolean click,flag;


    public Move(int row,int column){
        this.row = row;
        this.column = column;
        click = flag = false;
    }
}
