package MineSweeper;

import javax.xml.stream.events.Characters;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineSweeperSolver {
    private AiTile [][] grid;
    //just stores basic information for tile for AI to solve
    public class AiTile{
        int row,column;
        public boolean isOpen;
        public boolean isFlagged;
        public int numBombs;
        public int possibility = 0;



        public AiTile(){
            row=column=-1;
            isOpen = isFlagged = false;
            numBombs = 0;

        }

        public AiTile(int row,int column){
            isOpen = isFlagged = false;
            numBombs = 0;
            this.row=row;
            this.column=column;
        }
        //used for brutus func
        public AiTile(AiTile copy){
            this.isOpen = copy.isOpen;
            this.isFlagged = copy.isFlagged;
            this.row = copy.row;
            this.column = copy.column;
            this.numBombs = copy.numBombs;
        }
    }


    public void printGrid(){
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                if (grid[row][column].isOpen) {
                    if (grid[row][column].numBombs == 0) {
                        System.out.print(" ");
                    } else {
                        System.out.print(grid[row][column].numBombs);
                    }

                }
                //flagged so we mark as X
                else if (grid[row][column].isFlagged) {
                    System.out.print("X");
                }
                //closed so we mark as ?
                else if (grid[row][column].isOpen == false) {
                    System.out.print("?");
                }

            }
            System.out.println();
        }
    }

    //Loads in the grid to solve
    public void loadGrid(String input){

        int columnSize,rowSize;
        String [] temp = input.split("\n");
        rowSize = temp.length;
        columnSize = temp[0].length();
        grid = new AiTile[rowSize][columnSize];

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {

                //it is a space/empty tile
                if (temp[row].charAt(column) == ' ') {
                    grid[row][column] = new AiTile(row, column);
                    grid[row][column].isOpen = true;
                }

                //it is a numbered tile
                else if (temp[row].charAt(column) >= '0' && temp[row].charAt(column) <= '9') {
                    grid[row][column] = new AiTile(row, column);
                    grid[row][column].isOpen = true;
                    grid[row][column].numBombs = temp[row].charAt(column)-'0';
                }

                //it is a closed tile
                else if (temp[row].charAt(column) == '?') {
                    grid[row][column] = new AiTile(row, column);
                }

                //it is a flagged tile
                else if (temp[row].charAt(column) == 'X') {
                    grid[row][column] = new AiTile(row, column);
                    grid[row][column].isFlagged = true;
                }
                else{
                    throw new IndexOutOfBoundsException("No such symbol exists");
                }
            }
        }


    }

    //will return next pair of coordinates {x,y} to flag
    //or return null if not possible move/solved
    public ArrayList<Move> findNextFlag(){
        ArrayList<Move> ret = basicRule1();
        if(!ret.isEmpty()) return ret;
        else return null;
    }


    //will return next pair of coordinates {x,y} to flag
    //or return null if not possible move/solved
    public ArrayList<Move> findNextOpen(){
        ArrayList<Move> ret = basicRule2();
        if(!ret.isEmpty()) return ret;
        else return null;
    }


    //rule #1:
    //if the number of the tile
    //is the same as unknowns
    //then those unknowns are bombs
    private ArrayList <Move> basicRule1(){
        ArrayList<Move> ret = new ArrayList<>();
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                ArrayList <AiTile> neighbors = getNeighbors(row,column);

                for(int i = 0; i < neighbors.size();i++){
                    if(neighbors.get(i).isOpen){
                        neighbors.remove(i);
                        i--;
                    }
                }

                if(neighbors.size() == grid[row][column].numBombs){
                    for(AiTile t: neighbors){

                        //checks if tile has already been flagged
                        if(t.isFlagged){
                            continue;
                        }

                        //create move to add
                        Move temp = new Move(t.row,t.column);
                        temp.flag=true;

                        //checks if move already exists
                        boolean exists = false;
                        for(Move m: ret){
                            if(m.row == temp.row && m.column == temp.column){
                                exists = true;
                                break;
                            }
                        }

                        if(!exists){
                            ret.add(temp);
                            grid[row][column].isFlagged = true;
                        }



                    }
                }
            }
        }
        return ret;
    }
    //rule #2:
    //if there is a known tile with number X
    //and there are X flagged/known bombs
    //then the unknown tiles must be safe
    private ArrayList<Move> basicRule2(){

        ArrayList<Move> ret = new ArrayList<>();
        for (int row = 0; row < grid.length; row++) {
            for(int column = 0; column < grid[row].length; column++) {
                if(grid[row][column].numBombs == 0) continue;
                ArrayList<AiTile> neighbors = getNeighbors(row,column);
                int flagged = 0;

                //filtering neighbors so we dont consider what is already open or flagged
                //we keep track of flagged for analysis of rule #2
                for(int i = 0; i < neighbors.size();i++){
                    if(neighbors.get(i).isOpen || neighbors.get(i).isFlagged){
                        if(neighbors.get(i).isFlagged)flagged++;
                        neighbors.remove(i);
                        i--;
                    }
                }

                //number of flagged bombs is same as mine number
                //so rest must be safe
                if(flagged == grid[row][column].numBombs){
                    for(AiTile t: neighbors){

                        //create move to add
                        Move temp = new Move(t.row,t.column);
                        temp.click=true;

                        //checks if move already exists
                        boolean exists = false;
                        for(Move m: ret){
                            if(m.row == temp.row && m.column == temp.column){
                                exists = true;
                                break;
                            }
                        }

                        if(!exists){
                            ret.add(temp);
                            grid[row][column].isOpen = true;
                        }



                    }
                }

            }
        }

        return ret;
    }
    /*

    using DFS we'll have to go through all possibilities of an set of tiles
    Making educated guesses we can find out
    should only return either 100% chance of bomb/safe
    or return only 1 move with highest chance of safe

     */
    public ArrayList<Move> Brutus(boolean force){

        ArrayList<Move> ret = new ArrayList<>();

        //Holds the unknown nodes that are neighboring a known node
        ArrayList <AiTile> check = new ArrayList<>();

        //storing all combinations
        ArrayList<ArrayList<AiTile>> all;


        //we will add all possibilities
        for (int row = 0; row < grid.length; row++) {
            for(int column = 0; column < grid[row].length; column++) {

                //if it is open we will grab neighboring tile
                if(grid[row][column].isOpen){

                    ArrayList<AiTile> neighbors = getNeighbors(grid[row][column]);

                    //filters neighbors by unflagged and unopened
                    for(AiTile t: neighbors){

                        //will not add opened or flagged
                        if(t.isOpen || t.isFlagged){
                            continue;
                        }

                        //only adds unknown and if it hasnt been added before
                        if (check.indexOf(t)==-1){
                            check.add(t);
                        }
                    }
                }
            }
        }

        if( check.size() == 0) return null;

        //its a 50-50 chance regardless
        if(check.size() <= 2){
            Move temp = new Move(check.get(0).row,check.get(0).column);
            ret.add(temp);
            return ret;
        }

        //if it is higher than 15 it will take a while
        if((check.size()>15 && !force)){
            System.out.println("Checking approximately 2^"+check.size() + " are you sure?");
            return null;
        }


        //gets all possible combinations and filters them
        System.out.println("Check size: "+check.size());
        all = getAllPossibilities(check);
        System.out.println("Combinations BEFORE: "+all.size());
        filterPossibilities(all);
        System.out.println("Combinations After: " + all.size());

        //assigning possibilities
        for(ArrayList<AiTile> combination:all){
            for(AiTile tile: combination){
                if(findIndex(check,tile)!=-1){
                    if(tile.isFlagged)
                        check.get(findIndex(check,tile)).possibility++;
                }
            }
        }

        //checking for safest option
        AiTile min = check.get(0);
        AiTile max = check.get(0);
        for(AiTile t: check) {

            //checking if it is lowest possibility of bomb
            if (min.possibility > t.possibility) {
                min = t;
            }
            if(max.possibility<t.possibility){
                max=t;
            }

        }


        //if max and min are the same we will go through tiles and get one with lowest RNG chance
        if(min.possibility==max.possibility){

            for(AiTile t: check){
                if(getUnknown(min).size() < getUnknown(t).size()){
                    min = t;
                }
            }

        }

        //adding lowest chance
        Move temp = new Move(min.row,min.column);
        temp.click = true;
        ret.add(temp);





        //for seeing possibilities

        /*
        for(Move m: ret){
            System.out.print("Row: "+m.row+" Column: "+m.column);
            if(m.click){
                System.out.println(" Action: Click" );
            }else{
                System.out.println(" Action: Flag" );
            }
        }

         */
        for(AiTile t:check){
            System.out.println("Row: "+t.row+" Column: "+t.column + " Possibility: " + t.possibility+"/"+all.size());
        }



        return ret;
    }
    public void resetSimulation(ArrayList<AiTile> edge){
        for(AiTile t: edge){
            grid[t.row][t.column].isFlagged = false;
        }
    }

    public int findIndex(ArrayList<AiTile> AL, AiTile tile){
        for(int i = 0; i < AL.size();i++){
            if(AL.get(i).row == tile.row && AL.get(i).column==tile.column){
                return i;
            }
        }
        return -1;
    }

    public void filterPossibilities(ArrayList<ArrayList<AiTile>> possibilities) {

        for (int i = 0; i < possibilities.size(); i++) {
            ArrayList<AiTile> combination = possibilities.get(i);
            if (!isPossible(combination)) {
                possibilities.remove(combination);
                i--;
            }
        }
    }


    //will check the combination and if one tile is invalid due to
    //having flags and num bombs of a tile not being the same
    public boolean isPossible(ArrayList<AiTile> combination){

        //apply possibility
        for (AiTile t : combination) {
            grid[t.row][t.column].isFlagged = t.isFlagged;
        }

        //check possibility
        for(AiTile t: combination){
            ArrayList<AiTile> neighbor = getNeighbors(t);
            //remove unknown neighbors
            for(int i = 0; i < neighbor.size();i++){
                if(neighbor.get(i).isOpen==false){
                    neighbor.remove(neighbor.get(i));
                    i--;
                    continue;
                }
            }
            //checking how many neighbors have been flagged
            for(AiTile temp: neighbor){

                //getting neighbor of neighboring open tiles
                ArrayList<AiTile> neighbor2 = getNeighbors(temp);
                int flagged = 0;

                //checking flag counter
                for(AiTile temp2:neighbor2){
                    if(temp2.isFlagged){
                        flagged++;
                    }
                }

                //hypothetical flags not same as bomb count
                if (flagged != temp.numBombs){
                    resetSimulation(combination);
                    return false;
                }
            }
        }
        resetSimulation(combination);
        return true;
    }

    public boolean filterAdded(ArrayList<AiTile> combination) {

        //simulate flags
        for (AiTile t : combination) {
            grid[t.row][t.column].isFlagged = t.isFlagged;
        }

        for (AiTile t : combination) {

            //get neighbors of the unknown tile
            ArrayList<AiTile> neighbor = getNeighbors(t);

            //remove unknown neighbors
            for (int i = 0; i < neighbor.size(); i++) {
                if (neighbor.get(i).isOpen == false) {
                    neighbor.remove(neighbor.get(i));
                    i--;
                    continue;
                }
            }

            //checking how many neighbors have been flagged
            for (AiTile temp : neighbor) {

                //getting neighbor of neighboring open tiles
                ArrayList<AiTile> neighbor2 = getNeighbors(temp);
                int flagged = 0;

                //checking flag counter
                for (AiTile temp2 : neighbor2) {
                    if (temp2.isFlagged) {
                        flagged++;
                    }
                }

                if (flagged > temp.numBombs){
                    return false;
                }
            }
        }
        return true;
    }

    //initial call
    public ArrayList<ArrayList<AiTile>> getAllPossibilities(ArrayList<AiTile> edge){
        ArrayList<ArrayList<AiTile>> ret = new ArrayList<>();

        getPossibilitiy(ret,new ArrayList<AiTile>(),getDeepCopy(edge));
        return ret;
    }

    public ArrayList<AiTile> getDeepCopy(ArrayList<AiTile> toCopy){
        ArrayList<AiTile> ret = new ArrayList<>();
        for(AiTile t: toCopy){
            ret.add(new AiTile(t));
        }
        return ret;
    }

    //recursion to get all possibilities
    public void getPossibilitiy (ArrayList<ArrayList<AiTile>> All, ArrayList <AiTile> added, ArrayList<AiTile> unchecked){

        //we have checked all possibilities so we add combination to list
        if(unchecked.size()==0){
            ArrayList<AiTile> temp2 = new ArrayList<>();
            for(AiTile t: added){
                temp2.add(t);
            }
            All.add(temp2);
            return;
        }

        //from here on it is not possible (improve efficiency)
        if(filterAdded(added)==false){
            ArrayList<AiTile>reset = new ArrayList<>();
            reset.addAll(added);
            reset.addAll(unchecked);
            resetSimulation(reset);
            return;
        }
        ArrayList<AiTile>reset = new ArrayList<>();
        reset.addAll(added);
        reset.addAll(unchecked);
        resetSimulation(reset);

        //add marked tile and check
        AiTile temp = new AiTile(unchecked.get(0));
        temp.isFlagged = true;
        added.add(temp);
        ArrayList<AiTile>Temp2 = getDeepCopy(unchecked);
        Temp2.remove(0);
        getPossibilitiy(All,getDeepCopy(added),Temp2);

        //add unmarked tile and check
        added.remove(temp);
        temp = new AiTile(temp);
        temp.isFlagged = false;
        added.add(temp);
        getPossibilitiy(All,getDeepCopy(added),Temp2);
    }



    public ArrayList<AiTile> getUnknown(AiTile tile){
        ArrayList <AiTile>ret = getNeighbors(tile);

        for(int i = 0; i < ret.size();i++){
            if(ret.get(i).isOpen || ret.get(i).isFlagged){
                ret.remove(i);
                i--;
            }
        }
        return ret;
    }


    private ArrayList<AiTile> getNeighbors(AiTile tile){
        return getNeighbors(tile.row,tile.column);
    }
    private ArrayList<AiTile> getNeighbors(int row, int column){
        ArrayList<AiTile> neighbors = new ArrayList<>();
        int [] points = new int[]{
                -1,-1,
                -1,0,
                -1,1,
                0,-1,
                0,1,
                1,-1,
                1,0,
                1,1,
        };
        for(int i=0; i < points.length; i++){
            int rowOffset = points[i];
            int columnOffset = points[++i];

            int newRow = row+rowOffset;
            int newColumn = column+columnOffset;

            if(newColumn >= 0 && newColumn < grid[0].length
                    && newRow >=0 && newRow < grid.length){
                neighbors.add(grid[newRow][newColumn]);
            }
        }
        return neighbors;
    }





    
}
