package controller;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class MapInteractionHandler {
	
	//for the map interaction we have 

    private final Node inputNode; // take input from the centerMapStackPane
    private final Node targetNode; // output to the group of panes
    
    //panning variables
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    

    //zoom and rotate settings
	//booleans so that trackpads zoom and rotate feature dont get confused and jitter or glitches
    private boolean isRotating = false;
    private boolean isZooming = false;
    //zoom
    private static final double MAX_SCALE = 10.0;
    private static final double MIN_SCALE = 0.1;
    private static final double zoomFactor = 1.2;
    //rotate
    private static final double MOUSE_ROTATION_SENSITIVITY = 0.8; //degrees per pixel dragged

    public MapInteractionHandler(Node inputNode, Node targetNode) {
        this.inputNode = inputNode;
        this.targetNode = targetNode;
        addListeners();
    }

    private void addListeners() {
        inputNode.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();//this function get the current x on the whole scence
            mouseAnchorY = event.getSceneY();
            
            //this is the coordinates of the center of
            Point2D pointOnMap = targetNode.sceneToLocal(mouseAnchorX, mouseAnchorY);// this one is the coordinates of the map itself
            //if an edge is at 200,200 on the map, when you move the map somewhere else, that edge will still be at 200,200 on map
            //but on the scene it will be different
            
            //this twos say how far the map pane, (the paper) has been drifted away from the original point
            translateAnchorX = targetNode.getTranslateX();
            translateAnchorY = targetNode.getTranslateY();
            
            //just for understanding the documentation
//            System.out.println(mouseAnchorX);
//            System.out.println(mouseAnchorY);
//            System.out.println(translateAnchorX);
//            System.out.println(translateAnchorY);
//            System.out.println(""+pointOnMap+"\n--\n");
            
        });

        //drag or rotate with mouse, not trackpads
        inputNode.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {

                double deltaX = event.getSceneX() - mouseAnchorX;
                
                double angleDelta = deltaX * MOUSE_ROTATION_SENSITIVITY;
                
                // Rotate around the center of the screen (or mouse anchor)
                // Using mouseAnchor makes it feel like spinning a paper under your finger
                rotateAroundPivot(angleDelta, mouseAnchorX, mouseAnchorY);
                
                // Reset anchor so rotation doesn't accelerate wildly
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
            }
            //if left mouse -> drag
            else if (event.isPrimaryButtonDown()) {
                double deltaX = event.getSceneX() - mouseAnchorX;
                double deltaY = event.getSceneY() - mouseAnchorY;
                
                targetNode.setTranslateX(translateAnchorX + deltaX);
                targetNode.setTranslateY(translateAnchorY + deltaY);
            }
        });
        
        inputNode.setOnRotationStarted(e -> isRotating = true);
        inputNode.setOnRotationFinished(e -> isRotating = false);

        inputNode.setOnRotate(event -> {
        	//getAngle is to find the angle that the map pane has rotated from start to now
            rotateAroundPivot(event.getAngle(), event.getSceneX(), event.getSceneY());
            event.consume();
        });

        inputNode.setOnZoomStarted(e -> isZooming = true);
        inputNode.setOnZoomFinished(e -> isZooming = false);

        inputNode.setOnZoom(event -> {
            if (isRotating) return;

            zoomToPivot(event.getZoomFactor(), event.getSceneX(), event.getSceneY());
            event.consume();
        });
        inputNode.setOnScroll((ScrollEvent event) -> {
            if (isRotating || isZooming) {
                event.consume();
                return;
            }
            double newZoomFactor = zoomFactor;
            if (event.getDeltaY() < 0) {
                newZoomFactor = 1 / newZoomFactor; 
            }
            zoomToPivot(newZoomFactor, event.getSceneX(), event.getSceneY());
            event.consume();
        });
    }
    
    public void handleZoomIn() {
        Scene scene = inputNode.getScene();
        if (scene == null) return; 
        double screenCenterX = scene.getWidth() / 2;
        double screenCenterY = scene.getHeight() / 2;
        zoomToPivot(zoomFactor, screenCenterX, screenCenterY);
    }

    public void handleZoomOut() {
        Scene scene = inputNode.getScene();
        if (scene == null) return;

        double screenCenterX = scene.getWidth() / 2;
        double screenCenterY = scene.getHeight() / 2;

        zoomToPivot(1 / zoomFactor, screenCenterX, screenCenterY);
    }

    public void handleResetView() {
        this.targetNode.setScaleX(0.75);
        this.targetNode.setScaleY(0.75);
       
        this.targetNode.setTranslateX(0); 
        this.targetNode.setTranslateY(0);

        this.targetNode.setRotate(0);
    }
        

    
    private void zoomToPivot(double zoomFactor, double pivotSceneX, double pivotSceneY) {
    	//the zoom factor must be process to indicate zoom in or out, in means bigger zoom factor, out means smaller zoom factor
        //calculate the new zoom and add limit
        double currentScale = targetNode.getScaleX();
        double newScale = currentScale * zoomFactor;
        if (newScale > MAX_SCALE) newScale = MAX_SCALE;
        if (newScale < MIN_SCALE) newScale = MIN_SCALE;


        //from the xy of the mouse from scene, translate to local (ex: the local mouse on the map pane you want to focus on)
        //1. imagine you point at 200,200 on the scence and the local map, no zoom yet
        Point2D pivotOnMap = targetNode.sceneToLocal(pivotSceneX, pivotSceneY);
        
        //2. then you x2 the map, the edge you chose at 200,200 on scene now moves to 400,400 on scence 
        //but your mouse still at 200,200 on scence
        //then you scale the map pane, ex: if you zoom in, the edge you chose to pivot in the last step go far away, drifted away
        //this zoom is dumb, because it does not move the pane to the place under your mouse
        targetNode.setScaleX(newScale);
        targetNode.setScaleY(newScale);
        
        //now you want to find the new location of the edge you chose on the scene to move the pane there
        //which should give you 400x400
        Point2D newLocationInScene = targetNode.localToScene(pivotOnMap);

        //cal the drift
        double driftX = newLocationInScene.getX() - pivotSceneX;
        double driftY = newLocationInScene.getY() - pivotSceneY;
        
        //minus drift so it moves back to the mouse
        targetNode.setTranslateX(targetNode.getTranslateX() - driftX);
        targetNode.setTranslateY(targetNode.getTranslateY() - driftY);
    }
    
    private void rotateAroundPivot(double angleDelta, double pivotSceneX, double pivotSceneY) {
        Point2D pivotOnMap = targetNode.sceneToLocal(pivotSceneX, pivotSceneY);
        
        targetNode.setRotate(targetNode.getRotate() + angleDelta);
        
        Point2D newLocationInScene = targetNode.localToScene(pivotOnMap);
        double driftX = newLocationInScene.getX() - pivotSceneX;
        double driftY = newLocationInScene.getY() - pivotSceneY;

        targetNode.setTranslateX(targetNode.getTranslateX() - driftX);
        targetNode.setTranslateY(targetNode.getTranslateY() - driftY);
    }
    
}