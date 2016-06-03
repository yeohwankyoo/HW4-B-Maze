package com.example.yeohwankyoo.hw4_b;

/**
 * Created by Yeohwankyoo on 2016-06-03.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;

public class MazeActivity extends SurfaceView implements SurfaceHolder.Callback{
    Canvas cacheCanvas;
    Bitmap backBuffer;
    Bitmap mazeBuffer;
    Bitmap mario;
    Bitmap start;
    Bitmap finish;
    int width, height;
    Paint paint;
    Context context;
    SurfaceHolder mHolder;

    /////Mario////////////////////////
    boolean userTouch=false;           //check if touch character or not
    int [] marioLoaction = new int [2];  //Location of mario (x, y)

    /////Maze/////////////////////////
    int[][] maze = new int[13][9]; // The maze
    boolean[][] wasHere = new boolean[13][9];   //
    boolean[][] correctPath = new boolean[13][9]; //
    int startX=1, startY=1; // Starting X and Y values of maze
    int endX=7, endY=11;     // Ending X and Y values of maze

    boolean touchCheck=false;  // Check character(Mario) touch wall or not

    public MazeActivity(Context context) {
        super(context);
        GenMage();          //Random creation of Maze
        this.context = context;
        init();
    }public MazeActivity(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    //initial
    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        // wall image of maze
        mazeBuffer = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        mazeBuffer = Bitmap.createScaledBitmap(mazeBuffer, 120, 120, false);  // resizing 120 * 120

        //start point image
        start = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        start = Bitmap.createScaledBitmap(start, 120, 120, false);   // resizing 120 * 120

        //finish point image
        finish = BitmapFactory.decodeResource(getResources(), R.drawable.finish);
        finish = Bitmap.createScaledBitmap(finish, 120, 120, false);     // resizing 120 * 120

        //mario image
        mario = BitmapFactory.decodeResource(getResources(), R.drawable.mario);
        mario = Bitmap.createScaledBitmap(mario, 120, 120, false);       // resizing 120 * 120

        //Check path solution of maze
        while(true){
            GenMage();
            if(solveMaze() == true)
            {
                maze[11][7] = 0;
                break;
            }
        }


    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
    public void surfaceCreated(SurfaceHolder holder) {
        width = getWidth();
        height = getHeight();
        cacheCanvas = new Canvas();
        backBuffer = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(backBuffer);
        cacheCanvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        drawMap();
        cacheCanvas.drawBitmap(mario, 120, 120, null);
        marioLoaction[0]=120;       //location of mario (x value)
        marioLoaction[1]=120;       //location of mario (y value)
        draw();

    }
    //Draw wall of maze
    public void drawMap(){
        int nowLocationx=0,nowLocaiony=0;

        // 9 * 13 Maze
        for(int i=0;i<13;i++){
            for(int j=0;j<9;j++){
                if(maze[i][j] == 1)
                    cacheCanvas.drawBitmap(mazeBuffer, nowLocationx,nowLocaiony , null);
                nowLocationx+=120;
            }
            nowLocationx=0;
            nowLocaiony+=120;
        }

        //draw start point and finish point
        cacheCanvas.drawBitmap(start, 120, 120, null);
        cacheCanvas.drawBitmap(finish, 120*7, 120*11, null);

    }
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    int lastX, lastY, currX, currY;
    boolean isDeleting;

    //Touch Event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:               //When click
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                //if touch Mario, (character) then, state is true.
                if(marioLoaction[0]-60 < lastX &&lastX <  marioLoaction[0]+120 && marioLoaction[1]-60 < lastY && lastY < marioLoaction[1]+120 ){
                    userTouch=true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDeleting) break;

                currX = (int) event.getX();
                currY = (int) event.getY();

                //block touch invalid area
                if (currY/120 > 12) currY -= 120;
                if (currX/120 > 8) currX -= 120;
                if (currY/120  < 0) currY += 120;
                if (currX/120 < 0) currX += 120;

                //If chaeracter crash Wall,
                if(maze[currY/120][currX/120]==1 && userTouch==true) {
                    touchCheck = true;      //change state
                    userTouch = false;
                    init();
                    break;
                }
                cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);  // path of character

                //When character reach finish point
                if(lastX/120 ==7 && lastY/120 == 11 && userTouch == true)
                    Toast.makeText(getContext(), "Finish", Toast.LENGTH_SHORT).show();

                lastX = currX;
                lastY = currY;

                break;

            case MotionEvent.ACTION_UP:
                cacheCanvas.drawColor(Color.WHITE);
                drawMap();

                //If Mario crash wall,
                if (touchCheck) {
                    cacheCanvas.drawBitmap(mario, 120, 120, null);  //re-draw character at start point
                    marioLoaction[0]=120;               //initial X value
                    marioLoaction[1]=120;              // initial Y value
                    touchCheck = false;
                }
                else if(userTouch){
                    cacheCanvas.drawBitmap(mario,currX-60,currY-60, null);
                    marioLoaction[0]=currX-60;
                    marioLoaction[1]=currY-60;
                    userTouch=false;
                }else {
                    cacheCanvas.drawBitmap(mario, marioLoaction[0], marioLoaction[1], null);
                }

                break;
        }
        draw();
        return true;
    }
    protected void draw() {

        Canvas canvas = null;
        try{
            canvas = mHolder.lockCanvas(null);
//back buffer에 그려진 비트맵을 스크린 버퍼에 그린다
            canvas.drawBitmap(backBuffer, 0,0, paint);

        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            if(mHolder!=null) mHolder.unlockCanvasAndPost(canvas);
        }
    }

    ////////////Maze Algorithm //////////////////
    public boolean solveMaze() {
        //maze = generateMaze(); // Create Maze (0 = path, 1 = wall)
        for (int row = 0; row < 13; row++)
            // Sets boolean Arrays to default values
            for (int col = 0; col < 9; col++){
                wasHere[row][col] = false;
                correctPath[row][col] = false;
            }
        boolean b = recursiveSolve(startX, startY);
        // Will leave you with a boolean array (correctPath)
        // with the path indicated by true values.
        // If b is false, there is no solution to the maze
        return b;
    }
    public boolean recursiveSolve(int x, int y) {
        if (x ==endY && y == endX) return true; // If you reached the end
        if (maze[x][y] == 1 || wasHere[x][y]) return false;
        // If you are on a wall or already were here
        wasHere[x][y] = true;
        if (x != 0) // Checks if not on left edge
            if (recursiveSolve(x-1, y)) { // Recalls method one to the left
                correctPath[x][y] = true; // Sets that path value to true;
                return true;
            }
        if (x != 12) // Checks if not on right edge
            if (recursiveSolve(x+1, y)) { // Recalls method one to the right
                correctPath[x][y] = true;
                return true;
            }
        if (y != 0)  // Checks if not on top edge
            if (recursiveSolve(x, y-1)) { // Recalls method one up
                correctPath[x][y] = true;
                return true;
            }
        if (y != 8) // Checks if not on bottom edge
            if (recursiveSolve(x, y+1)) { // Recalls method one down
                correctPath[x][y] = true;
                return true;
            }
        return false;
    }

    //Make random Maze
    public void GenMage(){
        for(int i=0;i<13;i++){
            for(int j=0;j<9;j++)
            {
                if((i == 0 ) || ( i == 12 ) || ( j== 0 )||( j==8)){
                    maze[i][j]=1;
                }
                else{
                    if(i==1 && j==1)
                        maze[i][j]=0;
                    else if((int)(Math.random()*2) == 1)
                        maze[i][j]=1;
                    else
                        maze[i][j]=0;

                }
            }
        }


    }

} // class DrawingSurface
