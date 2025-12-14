package view;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer; // Để xử lý click chuột
import java.util.Map;
// --- JavaFX (Giao diện) ---
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow; // Hiệu ứng phát sáng
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;      // Vẽ ngã tư
import javafx.scene.shape.Polyline;    // Vẽ đường gấp khúc
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap; // Bo tròn đầu đường

// --- TraaS / SUMO (Thư viện mô phỏng) ---
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Lane;           // Lệnh lấy thông tin Lane
import de.tudresden.sumo.cmd.Junction;       // Lệnh lấy thông tin Junction
import de.tudresden.sumo.objects.SumoGeometry;   // Chứa danh sách tọa độ hình dáng
import de.tudresden.sumo.objects.SumoPosition2D; // Tọa độ X, Y lẻ
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightObject;
import model.vehicles.VehicleClass;
import util.ColorConverter;
import model.infrastructure.*;
// --- Project Classes (Các class của nhóm bạn) ---
import util.CoordinateConverter;
//import cần thiết cho đèn giao thông:
import de.tudresden.sumo.cmd.Trafficlight; // Lệnh lấy đèn
import de.tudresden.sumo.cmd.Junction;     // Lệnh lấy vị trí ngã tư
import javafx.scene.shape.Circle;          // Để vẽ hình tròn
	
// import cho vẽ xe
import de.tudresden.sumo.objects.SumoColor;     // Để hiểu màu sắc
import javafx.scene.shape.Polygon;   // vẽ hình 
import java.util.Map;
public class Renderer {
    public void setConverter(MapManager mapManager) {
        this.converter.setBound(mapManager);
    }
    public CoordinateConverter getConverter() {
        return this.converter;
    }
    
    private CoordinateConverter converter; 
    
    private static final DropShadow HOVER_GLOW = new DropShadow();
    
    private Map<Character, Color> tl_color_map = new HashMap<>(); // map the state of each traffic light to each color

    public Renderer() {
        // Tạo mới đối tượng converter khi Renderer được sinh ra
        this.converter = new CoordinateConverter(); 
        
        // Các cài đặt khác (Glow effect...)
        HOVER_GLOW.setColor(Color.CYAN);
        HOVER_GLOW.setRadius(10);
        HOVER_GLOW.setSpread(0.6);
        
    	//Khang's
  		this.tl_color_map.put('r', Color.rgb(255, 80, 80));            // bright_red

  		this.tl_color_map.put('y', Color.rgb(255, 255, 120));          // yellow

  		this.tl_color_map.put('g', Color.GREEN);                       // green
  		this.tl_color_map.put('G', Color.rgb(120, 255, 120));          // bright_green

  		// JavaFX has no blinking colors. You must implement blinking using transitions.
  		// Here: normal + brighter versions.
  		this.tl_color_map.put('o', Color.rgb(255, 200, 0));            // blinking_yellow (base)
  		this.tl_color_map.put('O', Color.rgb(255, 230, 50));           // bright_blinking_yellow (base)

  		this.tl_color_map.put('a', Color.rgb(139, 0, 0));              // dark_red (≈ Firebrick / DarkRed)
  		this.tl_color_map.put('b', Color.rgb(184, 134, 11));           // dark_yellow (≈ DarkGoldenRod)
  		this.tl_color_map.put('c', Color.rgb(0, 100, 0));              // dark_green (≈ DarkGreen)
    }



    /**
     * Renders all lanes onto the visualization map by categorizing them into specific UI layers (Panes)
     * based on their vehicle access permissions.
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Clears all existing children in car, bike, and mixed panes.</li>
     * <li>Iterates through the provided {@code laneData}.</li>
     * <li>Filters out internal junction lanes (IDs starting with ":").</li>
     * <li>Generates a {@link Shape} for each valid lane.</li>
     * <li>Assigns the shape to the appropriate pane:
     * <ul>
     * <li><b>Mixed Pane:</b> If both cars and bikes are allowed.</li>
     * <li><b>Bike Pane:</b> If only bikes are allowed.</li>
     * <li><b>Car Pane:</b> If only cars are allowed.</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * * <b>Note:</b> Lanes that do not fall into the above categories (e.g., restricted roads) 
     * are added to the {@code carPane} but set to be <b>mouse-transparent</b> (non-interactive).
     *
     * @param laneData A map containing lane IDs and their corresponding {@code LaneClass} properties.
     * @param onLaneClick A consumer callback to handle mouse click events on the generated lane shapes.
     */
    
    public void renderLanes(Map<String,LaneClass> laneData, Pane carPane, Pane bikePane,Pane mixedPane,Consumer<String> onLaneClick) {
    	
    	//should input list of
        // 1. Xóa sạch bản vẽ cũ
        carPane.getChildren().clear();
        bikePane.getChildren().clear();
        mixedPane.getChildren().clear();
        
        System.out.println("Renderer: Drawing lanes...");

        try {
            for (String laneId : laneData.keySet()) {
            	LaneClass props = laneData.get(laneId);
                // Bỏ qua lane nội bộ (ngã tư)
                if (laneId.startsWith(":")) continue;

                try {
//                 // CƠ CHẾ DỰ PHÒNG (Fallback)
//                    // Nếu một con đường lạ (bus, taxi, truck, delivery) không lọt vào danh sách trên,
//                    // ta mặc định ném nó vào pane Ô tô để nó HIỆN LÊN thay vì biến mất.
//                    if (!allowBike && !allowCar) {
//                        allowCar = true; 
//                    }
                	boolean allowBike = props.isBicycleAllowed();
                	boolean allowCar = props.isPassengerAllowed();
                    Shape laneShape = createLaneShape(props,laneData,onLaneClick);
                    if (laneShape != null) {
                        // CASE 1: Đường Hỗn Hợp (Cả 2 cùng đi được)
                        if (allowBike && allowCar) { 
                            mixedPane.getChildren().add(laneShape); // VÀO MIXED
                        }
                     // CASE 2: Chỉ cho Xe Đạp
                        else if (allowBike) {
                            bikePane.getChildren().add(laneShape); // VÀO BIKE
                        }
                        else if (allowCar) {
                            carPane.getChildren().add(laneShape); // VÀO CAR
                        }
                        //TRƯỜNG HỢP D: Các cái đường khác
                        else {
                          
                            // [QUAN TRỌNG NHẤT] Tắt tương tác chuột
                            laneShape.setMouseTransparent(true); 
                            
                            // Bỏ nó vào carPane (hoặc một pane nền nào đó)
                            // Vì MouseTransparent = true nên dù carPane có bật thì cũng không click vào hình này được
                            carPane.getChildren().add(laneShape);
                        }
                 
                     
                    }} catch (Exception e) {
                	e.printStackTrace();
                }
            }
            System.out.println("Renderer: Done drawing lanes.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructs a graphical representation (Shape) of a specific lane to be rendered on the map.
     * <p>
     * This method orchestrates the visual creation process by:
     * <ol>
     * <li>Retrieving the raw geometry (list of X, Y coordinates) from the SUMO simulation data.</li>
     * <li>Converting these real-world coordinates into JavaFX screen coordinates using the {@code converter}.</li>
     * <li>Creating a {@link Polyline} and applying visual styles (stroke width, color, line caps).</li>
     * <li>Attaching mouse interaction logic (Hover effects and Click delegation).</li>
     * </ol>
     * </p>
     *
     * @param props       The {@code LaneClass} object containing the lane's properties (ID, width, geometry shape).
     * @param laneData    The map containing data for all lanes (used for context if necessary).
     * @param onLaneClick A {@code Consumer} callback used to handle mouse click events.
     * <br><b>Note on Architecture:</b> This parameter facilitates a <i>delegation pattern</i>.
     * The {@code Renderer} detects the click event, but delegates the actual business logic 
     * (such as updating text fields in the UI) back to the {@code MainController} via this callback.
     * @return A fully styled {@link Shape} (specifically a {@link Polyline}) ready to be added to the UI pane, 
     * or {@code null} if the geometry data is invalid.
     */
    
    private Shape createLaneShape(LaneClass props, Map<String,LaneClass> laneData,Consumer<String> onLaneClick) {
    	//should input laneObject here
    	//Consumer là một type đặc biệt, nó dùng để lưu các dòng code chứ không phải chỉ là bién int, char,... bình thường.  
    	// Ở đây như kiểu là bạn được add cái function onLaneClick của MainController.java vào cái hàm này của bạn
    	//Tuy nhiên hiểu rõ hơn là MainController nó đang uỷ quyền cho cái Renderer của mình là: Này Renderer, tôi bận lắm không đứng canh chuột được. Cầm lấy cái lệnh này, bao giờ có ai click vào đường thì ông chạy cái lệnh này giúp tôi nhé!
    	
        try {
            // 1. Hỏi SUMO hình dáng của lane này (Trả về List tọa độ X,Y)
            SumoGeometry geometry = props.getShape();
            // SumoGeometry là một đối tượng chứa danh sách một loạt các điểm tọa độ (X, Y). Khi bạn nối các điểm này lại với nhau theo thứ tự, bạn sẽ tạo ra hình dáng của con đường.
            // Khi chạy lệnh kia bạn nhận về một túi chưa public List<SumoPosition2D> coords 
            // SumoPosition2D: Là một điểm, chứa x và y (tính bằng mét trong bản đồ thật).
            // coords: Là danh sách các điểm đó.
            //Lane.getShape(laneId): Đây là bạn viết một bức thư gửi SUMO: "Gửi SUMO, làm ơn cho tôi xin tọa độ hình dáng của làn đường tên là laneId".
            
            
           
            // 2. Tạo Polyline (Đường gấp khúc) của JavaFX
            Polyline lanePolyline = new Polyline();
            
            // 3. Duyệt qua các điểm tọa độ từ SUMO, convert sang JavaFX và thêm vào Polyline
            for (SumoPosition2D pos : geometry.coords) {
                // 1. Lấy điểm tọa độ thực (Mét)
                double realX = pos.x; 
                double realY = pos.y;

                // 2. Chuyển sang tọa độ màn hình (Pixel)
                double screenX = converter.toScreenX(realX);
                double screenY = converter.toScreenY(realY);

                // 3. Thêm điểm này vào đường gấp khúc (Polyline)
                lanePolyline.getPoints().addAll(screenX, screenY);
            }
            double laneWidth = props.getWidth();
            // 4. Style cho đường (Màu sắc, độ dày)
            lanePolyline.setStroke(Color.rgb(50, 50, 50)); // Màu đường nhựa
            lanePolyline.setStrokeWidth(laneWidth);   // Độ rộng đường (pixel) - có thể chỉnh theo zoom nếu muốn xịn
            lanePolyline.setStrokeLineCap(StrokeLineCap.ROUND);
            
            // Lưu ID vào UserData để sau này click vào biết là đường nào
            lanePolyline.setUserData(props.getId());

            // 5. Thêm hiệu ứng chuột (Logic cũ của bạn rất ổn!)
            lanePolyline.setOnMouseEntered(e -> {
                lanePolyline.setEffect(HOVER_GLOW);
                lanePolyline.setStroke(Color.LIGHTGRAY);
                lanePolyline.setCursor(Cursor.HAND);
            });
            lanePolyline.setOnMouseExited(e -> {
                lanePolyline.setEffect(null);
                lanePolyline.setStroke(Color.rgb(50, 50, 50)); // Trả về đúng màu gốc ban đầu
                lanePolyline.setCursor(Cursor.DEFAULT);
            });
            
         // [MỚI] Xử lý sự kiện Click dựa trên yêu cầu của MainController
            lanePolyline.setOnMouseClicked(e -> {
                if (onLaneClick != null) { 
                    // Lấy ID ra
                    String clickedId = (String) lanePolyline.getUserData();
                    // Kích hoạt hàm bên Controller (điền vào ô text field...)
                    onLaneClick.accept(clickedId);
                    //Trong Java, khi bạn dùng Consumer (người tiêu dùng), bản thân cái Consumer đó chỉ là một bọc chứa (một cái hộp/cái vỏ).
                    //Bên trong cái vỏ đó chứa một mệnh lệnh (đoạn code xử lý). Nhưng mệnh lệnh này nằm im, chưa chạy.
                    //Hàm accept() chính là hành động "Bấm Nút" hoặc "Kéo Cò" để mệnh lệnh đó thực sự chạy.
                    //Ý nghĩa: "Thực thi ngay đoạn code mà MainController đã gửi gắm, và dùng cái clickedId này làm nguyên liệu đầu vào cho đoạn code đó!"
                }
            });

            return lanePolyline;

        } catch (Exception e) {
            // Đôi khi có lane lỗi hoặc dữ liệu trống, bỏ qua
            return null; 
        }
    }


        /**
         * Renders all valid junctions (intersections) onto the visualization map.
         * <p>
         * <b>Processing Steps:</b>
         * <ol>
         * <li>Clears the {@code junctionPane} to remove old artifacts.</li>
         * <li>Iterates through the provided {@code junctionData}.</li>
         * <li><b>Filtering:</b> Skips internal SUMO junctions (IDs starting with ":") to avoid visual clutter.</li>
         * <li>Generates a geometric {@link Shape} for each valid junction.</li>
         * <li>Attaches a mouse click listener to delegate the selection event back to the Controller via {@code onJunctionClick}.</li>
         * <li>Adds the generated shape directly to the UI pane.</li>
         * </ol>
         * </p>
         *
         * @param junctionData    A map containing junction IDs and their properties (geometry, position).
         * @param junctionPane    The JavaFX {@link Pane} layer dedicated to displaying junctions.
         * @param onJunctionClick A callback (Consumer) to handle user interactions.
         * <br>When a user clicks a junction, its ID is passed to this consumer, allowing the MainController to react (e.g., show details).
         */
    public void renderJunctions(Map<String,JunctionClass>junctionData, Pane junctionPane, Consumer<String> onJunctionClick) {
        // 1. Tự dọn dẹp Pane trước khi vẽ
        junctionPane.getChildren().clear();

        try {
            for (String juncId : junctionData.keySet()) {
                // Bỏ qua ngã tư nội bộ
                if (juncId.startsWith(":")) continue;
                
                JunctionClass props = junctionData.get(juncId);
                // Tạo hình 
                Shape junctionShape = createJunctionShape(props);
                
                if (junctionShape != null) {
                    // Gắn sự kiện click
                    junctionShape.setOnMouseClicked(e -> {
                        if(onJunctionClick != null) onJunctionClick.accept(juncId);
                    });
                    
                    // 2. Vẽ TRỰC TIẾP vào Pane (thay vì add vào Group)
                    junctionPane.getChildren().add(junctionShape);
                }
            }
            System.out.println("Renderer: Done Drawing Junctions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructs the graphical representation (Polygon) for a single junction based on its geometry.
     * * <p>
     * This method converts real-world SUMO coordinates into screen pixel coordinates 
     * to form a closed {@link Polygon}.
     * </p>
     *
     * @param props The {@code JunctionClass} object containing the junction's ID and geometry shape.
     * @return A styled {@link Polygon} representing the intersection, ready for rendering.
     * Returns {@code null} if the junction has no geometry data or is invalid.
     */
    private Shape createJunctionShape(JunctionClass props) {
        try {
        	SumoGeometry geometry = props.getShape();
        	if (geometry == null || geometry.coords.isEmpty()) {
                return null; // Không có hình dáng thì bỏ qua
            }
        	// --- 2. Tạo một hình đa giác JavaFX (Polygon) ---
            Polygon junctionShape = new Polygon();
        	for (SumoPosition2D pos : geometry.coords) {
                // 1. Lấy điểm tọa độ thực (Mét)
                double realX = pos.x; 
                double realY = pos.y;

                // 2. Chuyển sang tọa độ màn hình (Pixel)
                double screenX = converter.toScreenX(realX);
                double screenY = converter.toScreenY(realY);

                // 3. Thêm điểm này vào đường gấp khúc (Polyline)
                junctionShape.getPoints().addAll(screenX, screenY);
            }
        	
        	junctionShape.setFill(Color.rgb(80, 80, 80)); // Màu xám đậm cho ngã tư
//            junctionShape.setStroke(Color.rgb(100, 100, 100)); // Viền
            junctionShape.setStrokeWidth(0.5);
            
            junctionShape.setUserData(props.getId());
            
            return junctionShape;
        } catch (Exception e) {
            return null;
        }
    }
    
    
    private Map<String, Polygon> vehicleVisualCache = new HashMap<>();
    
    /**
     * Renders and synchronizes the visual representation of vehicles on the map based on the latest simulation state.
     * <p>
     * <b>Performance Optimization Strategy:</b>
     * Instead of clearing and redrawing all vehicles every frame (which is computationally expensive), 
     * this method employs a <b>Caching Mechanism</b> ({@code vehicleVisualCache}) to synchronize the UI state:
     * <ul>
     * <li><b>Garbage Collection:</b> Identifies and removes vehicles that are present in the cache 
     * but no longer exist in the new {@code vehicleData} (i.e., vehicles that have left the simulation).</li>
     * <li><b>Update vs. Create:</b> 
     * <ul>
     * <li>If a vehicle ID exists in the cache, its existing {@link Polygon} shape is updated with new coordinates and rotation (Low cost).</li>
     * <li>If a vehicle ID is new, a new {@link Polygon} shape is instantiated, styled, and added to the cache (One-time cost).</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param vehiclePane The JavaFX {@link Pane} layer dedicated to displaying vehicles.
     * @param vehicleData A map containing the latest snapshot of vehicle data (ID -> Vehicle Properties) from the simulation core.
     */
	public void renderVehicles(Pane vehiclePane, Map<String, VehicleClass> vehicleData) {
        if (vehicleData == null || vehicleData.isEmpty()) {
            vehiclePane.getChildren().clear();  
            vehicleVisualCache.clear();
            return;
        }
        
     // CASE 2: CÓ DỮ LIỆU -> THỰC HIỆN ĐỒNG BỘ CACHE

        // --- A. XÓA XE ĐÃ BIẾN MẤT (GARBAGE COLLECTION) ---
        // Tìm những ID đang nằm trong Cache nhưng KHÔNG còn trong dữ liệu mới gửi về 
        List<String> toRemove = new ArrayList<>();
        for (String cachedId : vehicleVisualCache.keySet()) {
            if (!vehicleData.containsKey(cachedId)) { // nếu dữ liệu vehicleData gửi về không còn xe đó nữa thì xe đó cần phảị bị xoá 
                toRemove.add(cachedId);
            }
        }
        
     // Xóa thực sự
        for (String id : toRemove) {
            Polygon shape = vehicleVisualCache.get(id); // tạo biến shape này vì hàm remove của getChildren trong javafx nó cần 1 hình chứ không phải string
            vehiclePane.getChildren().remove(shape); // Gỡ cái xe có id đó khỏi giao diện. Ở đây phải dùng shape làolygonj bởi vì hàm remove nó cần mình đưa nó một Node (hình vẽ) chứ không phải 1 id. Polygon trong javafx là 1  
            vehicleVisualCache.remove(id);           // Xóa khỏi bộ nhớ đệm
        }
     // --- B. CẬP NHẬT HOẶC TẠO MỚI (UPDATE / CREATE) ---
        for (String vehicleId : vehicleData.keySet()) {
            VehicleClass props = vehicleData.get(vehicleId);
         // (Để tí nữa lấy cả cục props ra thì vẫn biết ID nó là gì)
            
            try {
            	// 1. LẤY DỮ LIỆU TỌA ĐỘ
	            double simX = 0;
	            double simY = 0;
	            double angle = 0;
	            Color carColor = Color.YELLOW;
	            // Lấy Tọa độ
	            SumoPosition2D posObj = props.getPosition(); // lấy cái dữ liệu từ cái Position đó 
	            simX = posObj.x; // gán simX là pos.x
	            simY = posObj.y; // gán simX là pos.y
	//          System.out.println(simX + " " + simY);
	//          Thread.sleep(1000);
	         // Chuyển đổi sang tọa độ màn hình để tí dùng. Toạ độ simX và simY không dùng được vì nó là toạ độ của SUMO, không khớp vs 
                double screenX = converter.toScreenX(simX);
                double screenY = converter.toScreenY(simY);
                
                // 2. LẤY DỮ LIỆU Angle
                angle = props.getAngle(); 
	                
	            
	            // 3. LẤY DỮ LIỆU Color
	            SumoColor color = props.getColor();      
	            // DÙNG HELPER ĐỂ CHUYỂN ĐỔI NGƯỢC LẠI
//	            System.out.println(colorObj);
	            carColor = ColorConverter.toFXColor(color);
//	            System.out.println(carColor);
	                

                // 4. KIỂM TRA TRONG CACHE
                Polygon carShape = vehicleVisualCache.get(vehicleId);

                if (carShape != null) {
                    // --- XE CŨ (ĐÃ CÓ) -> CHỈ CẬP NHẬT VỊ TRÍ ---
                    carShape.setTranslateX(screenX); //"Dịch chuyển" (Translate) toàn bộ hình vẽ đến một vị trí mới.
                    carShape.setTranslateY(screenY); //"Dịch chuyển" (Translate) toàn bộ hình vẽ đến một vị trí mới.
                    carShape.setRotate(angle); //Xoay hình vẽ quanh tâm của nó.
                    //Dữ liệu angle này lấy từ SUMO (thường SUMO tính góc 0 là hướng Bắc, quay chiều kim đồng hồ). JavaFX cũng xoay theo chiều kim đồng hồ, nên thường là tương thích tốt.
                    
                    // Cập nhật lại thông tin (để click vào ra info mới nhất)
                    carShape.setUserData(props); 
                    
                    // (Tùy chọn) Cập nhật màu nếu cần thiết
                    // updateVehicleColor(carShape, vehicleId);

                } else {
                    // --- XE MỚI (CHƯA CÓ) -> TẠO MỚI ---
                    carShape = new Polygon();
                    
                    // Vẽ hình tam giác
                  //Trong JavaFX, khi bạn tạo một Polygon (Đa giác), bạn cần cung cấp các cặp tọa độ (x, y) nối tiếp nhau. Tọa độ này tính từ tâm của chiếc xe (điểm 0,0).
                    double size = 2.0; // Kích thước xe (như code cũ của bạn)
                    carShape.getPoints().addAll(new Double[]{
    	            //.getPoints(): Lấy ra danh sách chứa các điểm tạo nên đa giác này (lúc đầu danh sách này rỗng).
    	            //Mảng new Double[]{...} của bạn chứa 6 số, tương ứng với 3 điểm (mỗi điểm gồm x và y):
    	                0.0, -size,    //y = -size: Nằm phía trên tâm (Trong JavaFX, trục Y hướng xuống dưới, nên số âm là đi lên).  
    	                -size/2, size,   //x = -size/2: Lệch sang trái một nửa kích thước., y = size: Nằm phía dưới tâm.
    	                size/2, size    //x = size/2: Lệch sang phải một nửa kích thước. y = size: Nằm phía dưới tâm.
    	            });

                    // Set vị trí ban đầu
                    carShape.setTranslateX(screenX);
                    carShape.setTranslateY(screenY);
                    carShape.setRotate(angle);
                    carShape.setFill(carColor);
                    carShape.setStrokeWidth(1);
                    // Lưu info
                    carShape.setUserData(props);
                    
                    // --- SỰ KIỆN CHUỘT (CHỈ CẦN GÁN 1 LẦN DUY NHẤT) ---
                    // Bạn không cần gán lại mỗi frame như cách cũ -> Tối ưu hơn nhiều
                    final Polygon finalShape = carShape; // mình phải tạo biến này thay vì dùng biến carShape ban đầu là vì biến carShape ban đầu nó là cái biến sẽ có sự thay đổi rất nhiều qua mỗi vòng for hoặc cập nhật.
              // Vấn đề nằm ở quy tắc của Java Lambda Expression (cái dấu ->).
                    //Bất kỳ biến nào nằm bên ngoài mà muốn chui vào trong Lambda (...) -> { ... } để sử dụng thì biến đó phải là FINAL (Bất di bất dịch, không được phép thay đổi giá trị sau khi khởi tạo)
                    // Lúc này finalShape được coi là "Effectively Final" (Chắc chắn không đổi), và Java cho phép mang nó vào trong sự kiện Click để dùng.
                    carShape.setOnMouseClicked(e -> {
                    	VehicleClass info =(VehicleClass) finalShape.getUserData();
                        System.out.println("Clicked Vehicle: " + info.getId());
                    });
                    
                    carShape.setOnMouseEntered(e -> {
                        finalShape.setEffect(HOVER_GLOW);
                        finalShape.setCursor(Cursor.HAND); //biến con trỏ chuột thành hình Bàn Tay
                    });
                    
                    carShape.setOnMouseExited(e -> {
                        finalShape.setEffect(null);
                        finalShape.setCursor(Cursor.DEFAULT);
                    });

                    // Add vào Pane và lưu vào Cache
                    vehiclePane.getChildren().add(carShape);
                    vehicleVisualCache.put(vehicleId, carShape);
                }

            } catch (Exception e) {
                System.err.println("Error rendering vehicle: " + vehicleId);
                continue;
            }
        }
    }
    
    
	
	
	/**
     * Clears the internal cache of vehicle visual objects.
     * <p>
     * This method removes all stored vehicle shapes ({@link Polygon}) from the {@code vehicleVisualCache}.
     * <b>Usage:</b> This should be called explicitly when:
     * <ul>
     * <li>Resetting or restarting the simulation.</li>
     * <li>Loading a new map.</li>
     * </ul>
     * This ensures that no stale visual artifacts ("ghost vehicles") from the previous session remain in memory or on screen.
     * </p>
     */
	public void clearVehicleCache() {
        this.vehicleVisualCache.clear();
    }
	

		public void renderTrafficLights(Pane trafficLightPane, Map<TrafficlightObject,Character>trafficLightsData, Consumer<TrafficlightObject> onTrafficLightClick) {
			
			if (trafficLightsData == null || trafficLightsData.isEmpty()) {
		        System.out.println("Empty traffic light map");
		        return;
		    }

		    // Check if traffic lights already exist
		    if (trafficLightPane.getChildren().isEmpty()) {
		        // First time: create all traffic lights
		        for (TrafficlightObject tl_link : trafficLightsData.keySet()) {
		            Character tl_color_char = trafficLightsData.get(tl_link);
		            try {
		                SumoPosition2D pos = tl_link.get_position();
		                double screenX = converter.toScreenX(pos.x);
		                double screenY = converter.toScreenY(pos.y);

		                Group lightGroup = new Group();
		                // Housing
		                Rectangle box = new Rectangle(-0.75, -2.125, 1.5, 4.25);
		                box.setArcWidth(0.75);
		                box.setArcHeight(0.75);
		                box.setFill(Color.rgb(30, 30, 30));
		                box.setStroke(Color.BLACK);

		                // Circles
		                Circle redLamp = new Circle(0, -1.125, 0.5);
		                Circle yellowLamp = new Circle(0, 0, 0.5);
		                Circle greenLamp = new Circle(0, 1.125, 0.5);

		                redLamp.setId("red");
		                yellowLamp.setId("yellow");
		                greenLamp.setId("green");

		                lightGroup.getChildren().addAll(box, redLamp, yellowLamp, greenLamp);
		                lightGroup.setTranslateX(screenX);
		                lightGroup.setTranslateY(screenY);
		                lightGroup.setUserData(tl_link);

		                // Click & Hover
		                lightGroup.setOnMouseClicked(e -> {
		                    if (onTrafficLightClick != null) {
		                        onTrafficLightClick.accept(tl_link);
		                    }
		                });
		                lightGroup.setOnMouseEntered(e -> {
		                    lightGroup.setEffect(HOVER_GLOW);
		                    lightGroup.setCursor(Cursor.HAND);
		                });
		                lightGroup.setOnMouseExited(e -> {
		                    lightGroup.setEffect(null);
		                    lightGroup.setCursor(Cursor.DEFAULT);
		                });

		                trafficLightPane.getChildren().add(lightGroup);

		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
		    }

		    // Update colors of all existing traffic lights
		    for (var node : trafficLightPane.getChildren()) {
		        if (!(node instanceof Group)) continue;
		        Group lightGroup = (Group) node;
		        TrafficlightObject tl_link = (TrafficlightObject) lightGroup.getUserData();
		        Character tl_color_char = trafficLightsData.get(tl_link);
		        if (tl_color_char == null) continue; // skip if no data

		        for (var child : lightGroup.getChildren()) {
		            if (!(child instanceof Circle)) continue;
		            Circle lamp = (Circle) child;
		            switch (lamp.getId()) {
		                case "red" -> {
		                    lamp.setFill((tl_color_char == 'r') ? tl_color_map.get(tl_color_char) : tl_color_map.get('a'));
		                }
		                case "yellow" -> {
		                    lamp.setFill((tl_color_char == 'y' || tl_color_char == 'O' || tl_color_char == 'o') ? tl_color_map.get(tl_color_char) : tl_color_map.get('b'));
		                }
		                case "green" -> {
		                    lamp.setFill((tl_color_char == 'G' || tl_color_char == 'g') ? tl_color_map.get(tl_color_char) : tl_color_map.get('c'));
		                }
		            }
		        }
		    }
		}
}