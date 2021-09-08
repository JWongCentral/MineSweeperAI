package MineSweeper;

import com.sun.xml.internal.ws.util.StringUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import javax.swing.Timer;
import java.io.FileInputStream;
import java.sql.Ref;
import java.util.*;

/*
Using depth first search to write down every possibility of each bomb that could occur
 */
public class Controller extends Application {
    private static Stage stage;
    private static final int Tile_Size = 20;
    private static final int width = 800, height = 1000;
    private static final int bombs = 300;
    private static int flaggedBombs = 0, usedFlags = 0;
    private static int X_Tiles = 40, Y_Tiles = 40;
    private static boolean started = false, running = true;
    private static boolean timerRunning = false;
    private static int timeCounter = 0, bombCounter = bombs;
    private Tile[][] grid = new Tile[X_Tiles][Y_Tiles];
    private static boolean win = false, lose = false;
    private Label timerLabel = new Label(), bombCounterLabel = new Label();
    private Tile [][] game;
    private Timeline timeline;
    private List<List<Tile>> Combinations = new ArrayList<>();

    private Parent createScene(){
        Pane root = new Pane();
        root.setPrefSize(width,height);
        if(!started) {
            for (int y = 0; y < Y_Tiles; y++) {
                for (int x = 0; x < X_Tiles; x++) {
                    Tile tile = new Tile(x, y, false);

                    grid[x][y] = tile;
                    root.getChildren().add(tile);
                }
            }
        }else{
            for (int y = 0; y < Y_Tiles; y++) {
                for (int x = 0; x < X_Tiles; x++) {
                    root.getChildren().add(grid[x][y]);
                }
            }
        }

        /*
        insert Top bomb counter and timer and retry button in the middle
         */

        Rectangle menu = new Rectangle(800,200);
        menu.setFill(Color.LIGHTGRAY);
        menu.setStroke(Color.BLACK);
        menu.setStrokeWidth(5);
        Rectangle textBorder = new Rectangle(100,30);
        textBorder.setFill(Color.BLACK);
        textBorder.setStroke(Color.DARKRED);
        textBorder.setStrokeWidth(5);
        textBorder.setTranslateX(100);
        textBorder.setTranslateY(100);
        timerLabel.setMaxSize(100,100);
        timerLabel.setText(cleanUp(timeCounter));
        bombCounterLabel.setText(""+bombCounter);
        timerLabel.setFont(Font.font(24));
        timerLabel.setTextFill(Color.GREEN);
        timerLabel.setTranslateX(100);
        timerLabel.setTranslateY(100);
        timerLabel.setVisible(true);
        startTimer();

        /*
        Create Bomber Counter
         */
        Rectangle bombBorder = new Rectangle(100,30);
        bombBorder.setFill(Color.BLACK);
        bombBorder.setStroke(Color.DARKRED);
        bombBorder.setStrokeWidth(5);
        bombBorder.setTranslateX(600);
        bombBorder.setTranslateY(100);
        bombCounterLabel.setText(cleanUp(bombCounter));
        bombCounterLabel.setTextFill(Color.GREEN);
        bombCounterLabel.setFont(Font.font(24));
        bombCounterLabel.setMaxSize(100,100);
        bombCounterLabel.setTranslateX(600);
        bombCounterLabel.setTranslateY(100);
        Button restartButton = null;
        try {
            restartButton = new Button();
            Image image = new Image(new FileInputStream("C:\\Users\\Jack PC\\IdeaProjects\\ProjectGui\\src\\MineSweeper\\smiley.png"));
            ImageView view = new ImageView(image);
            view.setFitHeight(50);
            view.setFitWidth(50);
            restartButton.setMaxSize(50,50);
            restartButton.setGraphic(view);
            restartButton.setTranslateY(90);
            restartButton.setTranslateX(375);
            restartButton.setOnAction(event -> resetGame());
        } catch (Exception e){
            System.out.println(e);
        }

        //insert lose
        if(lose){
            stopTimer();
            menu.setFill(Color.RED);
        }
        //insert win
        if(win){
            stopTimer();
            menu.setFill(Color.GREEN);
        }

        Button startAI = new Button("Start AI");
        startAI.setTranslateY(10);
        startAI.setTranslateX(10);
        startAI.setTextFill(Color.RED);
        startAI.setOnAction(event -> startAI());
        root.getChildren().addAll(menu,textBorder);
        if (restartButton!=null)
            root.getChildren().addAll(restartButton);
        root.getChildren().addAll(timerLabel);
        root.getChildren().addAll(bombBorder);
        root.getChildren().addAll(bombCounterLabel);
        root.getChildren().addAll(startAI);
        return root;
    }
    private List<Tile> getNeighbors(Tile[][] list,Tile tile){
        List<Tile> neighbors = new ArrayList<>();
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
            int dx = points[i];
            int dy = points[++i];

            int newX = tile.x+dx;
            int newY = tile.y+dy;

            if(newX >= 0 && newX < X_Tiles
                    && newY >=0 && newY < Y_Tiles){
                neighbors.add(list[newX][newY]);
            }
        }
        return neighbors;
    }

    public List<Tile> getNeighbors(Tile tile){
        //get neighbors of this tile
        List<Tile> ret = new ArrayList<>();

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
            int dx = points[i];
            int dy = points[++i];

            int newX = tile.x+dx;
            int newY = tile.y+dy;

            if(newX >= 0 && newX < X_Tiles && newY >=0 && newY < Y_Tiles){
                ret.add(grid[newX][newY]);
            }
        }
        return ret;
    }
    public List<Tile> getFilteredNeighbors(Tile tile){
        /*
        filter the neighboring tiles so we get the unflagged, unopened, and unsolved tiles.
        Basically getting the list of unknown tiles around a known tile
         */
        List<Tile> ret = new ArrayList<>();
        for (Tile t: getNeighbors(tile)) {
            if(t.solved == false && t.isOpen==false) {
                ret.add(t);
            }
        }
        return ret;
    }
    public void updateGameGrid(){
        game = new Tile[X_Tiles][Y_Tiles]; //var to hold what the game currently sees
        Tile tile;
        for(int x = 0; x < grid.length; x++){
            for(int y = 0; y < grid[x].length;y++){
                tile = grid[x][y];
                if(tile.isOpen){
                    game[x][y] = grid[x][y];
                    if(tile.text.getText()==null){
                        grid[x][y].solved = true; //if the tile is empty and opened it will be solved
                    }
                }
                else{
                    game[x][y] = null;
                }
            }
        }
    }
    public void startAI(){
        //called when u press the auto solve button
        updateGameGrid();
        /*
        we want to use the basic rules of mine sweeper until there are no cases where we can apply them before we can use the
        A* search algorithm
         */
        boolean isChanged;
        do {
            isChanged = false;
            List <Tile> neighbors;
            updateGameGrid();
            for(int x= 0; x < game.length; x++){
                for(int y = 0; y < game[x].length;y++) {
                /*
                Will go through all the tiles and try to find the unsolved tiles that are also open and check the neighboring tiles that
                also closed
                Check whether or not this tile goes through the basic rules
                 */
                    if (game[x][y] != null) {
                        if (game[x][y].isOpen == true && game[x][y].solved == false) {
                            neighbors = getFilteredNeighbors(game[x][y]);
                            if (BasicRules(game[x][y], neighbors) == true) {
                                isChanged = true;
                                continue;
                            }

                            else if (bruteForceMethod(game[x][y]) == true) {
                                isChanged = true;
                                continue;
                            }


                        }
                    }
                }
            }
        } while(isChanged);
    }

    /*
    checks if basic rules applies to this node.
    If it has solved the node it will return true
    Otherwise it will return false when it is not
     */
    public boolean BasicRules(Tile known, List<Tile> unknown){
        /*rule #1 reduce the number by knowing the flagged tiles
        if there are X number of flagged tiles around the known tile then we can safely deduce that the number of bombs around that
        tile is the number-X
        rule #2 if the number of tiles around the bomb is the same as the number then all the tiles around that tile are bombs
         */
        if(known.text.getText() == ""){
            return false;
        }
        int size = Integer.parseInt(known.text.getText());
        int flagged = 0;
        for(Tile e: unknown){
            if (e.isFlagged == true){
                flagged++;
            }
        }
        /*
        2 cases will emerge here
        when the size is > 0 that means that there are unknown bombs around it.
        When it is exactly 0 that means the unknowns are not bombs and can be open
         */
        if(size-flagged == 0){
            for(Tile e: unknown){
                if(e.isFlagged == false){
                    if(grid[e.x][e.y].isOpen == false) {
                        grid[e.x][e.y].open();
                        return true;
                    }
                }
            }
        }
        else if (size == unknown.size()){
            //when the number on the tile is the same as the unknown tiles then all must be bombs
            for(Tile e: unknown){
                if(grid[e.x][e.y].isFlagged == false) {
                    grid[e.x][e.y].flag();
                    return true;
                }
            }
        }

        return false;
    }

    public boolean bruteForceMethod(Tile mainTile){
        /*
        note: will always open a tile based on chance

        note: it is hard to create a 100% algorithm to work all the time as minesweeper is also a game of luck/chance
        therefore probability will always be a big part of solving game


        we run through all the possible permutations of a group of tiles
            -the main node which can't be solved through basic means
            -the neighboring opened tiles that have numbers
            -the unknown neighboring tiles of the main node
            -and the unknown of the main node's neighbors
        we run the possible bomb placements that CAN occur divided by the total bomb placements that can occur at that one node
        if the probability is low we can deduce it is safe of probability
        Steps:
        first we grab the opened and unsolved main tile
        second we grab the neighboring opened tiles
        third we get the unknown tiles that are the neighbor of each unknown tile
        fourth we run through the possible permutation
            -set up basic rules that can't be broken
            -if broken then we can throw this permutation out since it cannot occur
        fifth we combine the possible bomb locations and compute probability
        sixth we open tile
         */
        List<Tile> KnownTiles = new ArrayList<>();
        List<Tile> UnknownTiles = new ArrayList<>();
        for(Tile t:getNeighbors(mainTile)){
            if(t.isOpen == true && t.solved == false) {
                KnownTiles.add(t.getCopy());

                for (Tile e : getNeighbors(t)) {
                    //System.out.println(containsTile(KnownTiles, e) + " " + e.x + "," + e.y);
                    if (e.isOpen == false) {
                        UnknownTiles.add(e.getCopy());
                    }
                }
            }
        }
        List<Tile> data;
        List<Tile> reference = data = UnknownTiles;
        Combinations.clear();
        findCombination(Combinations, data, reference, 0, UnknownTiles.size());

        /*
        Add in check for permutation for the combinations
        ideas:
            1. create a new grid and force the combinations into the grid
                terrible run time; might end up taking forever
            2.get neighbors for the solved ones and fill in the specific x,y grid
                that the combinations will fill
                Implementing this one ^^
         */
        Tile [][] temp = game;
        for(List<Tile> comb : Combinations){
            for(Tile t: comb){
                //applies the combination to the game
                temp[t.x][t.y] = t;
            }

            for(Tile t: KnownTiles) {
                //Start of loop going throug known tile neighbors
                temp[t.x][t.y] = t;
                /*
                add in check statement here to see if the Tile is calid
                 */

                /*
                might have to create new functions to revolve around checking temp
                 */



            }


        }




        return false;

    }


    //For testing purposes only
    public void printList (List<Tile> list){
        for(Tile t: list){

            System.out.print("["+t.isFlagged + "]");
        }
        System.out.println();
    }
    public void findCombination(List<List<Tile>> combinations, List<Tile> data, List<Tile> Reference, int index, int depth){
        /*
        Using a recursive stack to create all posibilities and store it as a list of list
        First we populated the data and reference with all the unflagged tiles we use reference to get the value from unknown tiles
        Without changing the values of Unknown Tiles.
        We start by populating the data variable with the tiles, first starting as index 1 true and false and then multiplying as it
        goes down.
         */
        if(index == depth){

            List<Tile> nData = new ArrayList<>();
            Tile nTile;
            for(Tile t: data){
                nTile = new Tile (t.x,t.y);
                nTile.isFlagged = t.isFlagged;
                nData.add(nTile);
            }
            Combinations.add(nData);
            return;
        }
        else{
            Tile tile = Reference.get(index).getCopy();
            tile.isFlagged = true;
            data.set(index, tile);
            findCombination(combinations, data, Reference, (index+1), depth);
            tile = Reference.get(index).getCopy();
            tile.isFlagged = false;
            data.set(index, tile);
            findCombination(combinations, data, Reference, (index+1), depth);
        }


    }

    public void startTimer(){
        if (!timerRunning && !win && !lose){
            timerRunning = !timerRunning;
            timeline = new Timeline(
                    new KeyFrame(
                            Duration.ZERO,
                            event -> timerLabel.setText(cleanUp(++timeCounter))
                    ),
                    new KeyFrame(
                            Duration.seconds(1)
                    )
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }
    public void stopTimer(){
        timeline.stop();
    }
    public void resetGame(){
        win = lose = false;
        started = false;
        timeCounter = 0;
        bombCounter = bombs;
        flaggedBombs = 0;
        try {
            start(stage);
        }catch (Exception e){
            System.out.print(e);
            System.exit(-1);
        }
    }

    private String cleanUp(int num){
        //pad numbers so it generates 7 "0" digits withe the right most being replaced by num
        String numb = ""+num;
        String ret = String.format("%0" + 7 + "d",0);
        ret = ret.substring(0,7-numb.length()) + numb;
        return ret;
    }
    private int getBombs(List<Tile> t){
        int ret = 0;
        for(Tile e: t){
            if(e.hasBomb){
                ret++;
            }
        }
        return ret;
    }

    public class Tile extends StackPane {
        public boolean solved = false; //used for AI
        int x,y;
        private boolean hasBomb;
        public boolean isOpen = false;
        public boolean isFlagged = false;
        private Rectangle border = new Rectangle(Tile_Size-2,Tile_Size-2);
        private Text text = new Text();
        /*
        Constructor for deep copy without exposing bomb
         */
        public Tile (int x,int y){
            this.x = x;
            this.y = y;

        }
        public Tile(int x, int y, boolean bomb) {
            this.x = x;
            this.y = y;
            hasBomb = bomb;
            border.setStroke(Color.DARKGREEN);
            border.setFill(Color.DARKSLATEGRAY);
            text.setFont(Font.font(18));
            text.setVisible(false);
            text.setText("");

            getChildren().addAll(border, text);
            setTranslateX(x * Tile_Size);
            setTranslateY(y * Tile_Size+200);


            setOnMouseClicked(e -> {
                if(!started){
                    gameStart(x,y);
                    open();

                }else if (e.getButton() == MouseButton.PRIMARY) {
                    open();
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    flag();
                }
            });


        }

        public void flag(){
            if(!isOpen && !isFlagged && bombCounter != 0) {
                isFlagged = true;
                border.setFill(Color.DARKRED);
                usedFlags++;
                bombCounter--;
                bombCounterLabel.setText(cleanUp(bombCounter));
                if(hasBomb){
                    flaggedBombs++;
                    if(flaggedBombs == bombs){
                        win = true;
                        try{
                            start(stage);
                        } catch (Exception e){
                            System.out.println(e);
                        }
                    }
                }
            }
            else if(isFlagged&& !isOpen){
                isFlagged = false;
                bombCounter++;
                bombCounterLabel.setText(cleanUp(bombCounter));
                if(hasBomb) {
                    flaggedBombs--;
                }
                border.setFill(Color.DARKSLATEGRAY);
            }
        }

        public boolean compare(Tile t){
            return ((t.x == this.x) && (t.y == this.y));
        }

        public void gameStart(int x, int y){
            //make sure game doesnt start on a bomb
            if(started){
                return;
            }
            Random rand = new Random();
            int randX,randY, bombNum;
            bombNum = bombs;

            while(bombNum > 0){
                randX = rand.nextInt(X_Tiles);
                randY = rand.nextInt(Y_Tiles);
                //checks if its not within 3 blocks of x,y
                if (!(randX > (x-3) && randX<(x+3) && randY > (y-3) && randY <(y+3)) && grid[randX][randY].hasBomb==false){
                    grid[randX][randY].giveBomb();
                    bombNum--;
                }
            }
            int bombCount = 0;
            for(int x1 = 0; x1 < grid.length; x1++){
                for(int y1= 0; y1 < grid[x1].length;y1++){
                    if(!grid[x1][y1].hasBomb) {
                        bombCount = getBombs(getNeighbors(grid[x1][y1]));
                        if (bombCount != 0){
                            grid[x1][y1].text.setText(""+bombCount);
                        }
                    }
                }
            }
            started = true;


        }
        public void giveBomb(){
            hasBomb = true;
            text.setText("X");
        }
        public void open(){
            started = true;
            if (isOpen || isFlagged){
                return;
            }
            if(hasBomb){
                lose = true;
                try {
                    start(stage);
                } catch (Exception e ){
                    System.out.println(e);
                }

            }
            isOpen = true;
            text.setVisible(true);
            border.setFill(Color.LIGHTGRAY);
            if(text.getText().isEmpty()){
                getNeighbors(this).forEach(Tile::open);
            }
        }
        private List<Tile> getNeighbors(Tile tile){
            List<Tile> neighbors = new ArrayList<>();
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
                int dx = points[i];
                int dy = points[++i];

                int newX = tile.x+dx;
                int newY = tile.y+dy;

                if(newX >= 0 && newX < X_Tiles
                        && newY >=0 && newY < Y_Tiles){
                    neighbors.add(grid[newX][newY]);
                }
            }
            return neighbors;
        }


        public Tile getCopy(){
            Tile ret = new Tile(this.x,this.y,this.hasBomb);
            ret.solved = this.solved;
            ret.isOpen = this.isOpen;
            ret.border = this.border;
            ret.text = this.text;
            return ret;
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        Scene scene = new Scene(createScene());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
