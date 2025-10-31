package exe.SonMaiHeritage.config;

import exe.SonMaiHeritage.entity.*;
import exe.SonMaiHeritage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Data Seeder for Son Mai Heritage Application
 * This class automatically seeds the database with sample data on application startup
 * Only runs if the database is empty
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TypeRepository typeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.data.seeding.enabled:true}")
    private boolean seedingEnabled;
    
    @Value("${app.data.seeding.skip-products:false}")
    private boolean skipProducts;

    @Override
    public void run(String... args) throws Exception {
        if (!seedingEnabled) {
            log.info("Data seeding is disabled via configuration. Skipping all seeding operations.");
            return;
        }
        
        if (shouldSeedData()) {
            log.info("Starting database seeding...");
            seedProductTypes();
            
            // Chỉ tạo sản phẩm nếu không bị tắt
            if (!skipProducts) {
                seedProducts();
            } else {
                log.info("Product seeding is disabled. Skipping products.");
            }
            
            seedUsers();
            seedAddresses();
            log.info("Database seeding completed successfully!");
        } else {
            log.info("Database already contains data. Skipping seeding.");
        }
    }

    private boolean shouldSeedData() {
        return typeRepository.count() == 0 && userRepository.count() == 0;
    }

    private void seedProductTypes() {
        log.info("Seeding product types...");
        
        List<String> typeNames = Arrays.asList(
            "Áo dài truyền thống",
            "Trang sức bạc", 
            "Phụ kiện thổ cẩm",
            "Quần áo lụa",
            "Gốm sứ truyền thống",
            "Đồ gỗ thủ công",
            "Tranh dân gian",
            "Tranh sơn mài",
            "Đồ thêu",
            "Mũ nón lá",
            "Giày dép thủ công"
        );

        for (String typeName : typeNames) {
            Type type = Type.builder()
                    .name(typeName)
                    .build();
            typeRepository.save(type);
        }
        
        log.info("Seeded {} product types", typeNames.size());
    }

    private void seedProducts() {
        log.info("Seeding products...");
        
        List<Type> types = typeRepository.findAll();
        
        // Áo dài truyền thống
        Type aoDaiType = types.stream().filter(t -> t.getName().equals("Áo dài truyền thống")).findFirst().orElse(null);
        if (aoDaiType != null) {
            createProduct("Áo dài lụa tơ tằm Hà Nội", 
                "Áo dài truyền thống được may từ lụa tơ tằm cao cấp với họa tiết hoa sen tinh tế", 
                2500000L, getPlaceholderImageUrl(), aoDaiType);
            
            createProduct("Áo dài gấm Huế", 
                "Áo dài hoàng gia phong cách Huế với chất liệu gấm thêu rồng phượng", 
                3200000L, getPlaceholderImageUrl(), aoDaiType);
        }

        // Trang sức bạc
        Type silverType = types.stream().filter(t -> t.getName().equals("Trang sức bạc")).findFirst().orElse(null);
        if (silverType != null) {
            createProduct("Dây chuyền bạc hình rồng", 
                "Dây chuyền bạc ta thủ công với thiết kế rồng bay, biểu tượng quyền lực", 
                850000L, "getPlaceholderImageUrl()silver-dragon.jpg", silverType);
            
            createProduct("Nhẫn bạc khắc chữ Hán", 
                "Nhẫn bạc với chữ Hán cổ, mang ý nghĩa phong thủy tốt lành", 
                420000L, "getPlaceholderImageUrl()silver-ring.jpg", silverType);
        }

        // Phụ kiện thổ cẩm
        Type thoCamType = types.stream().filter(t -> t.getName().equals("Phụ kiện thổ cẩm")).findFirst().orElse(null);
        if (thoCamType != null) {
            createProduct("Túi xách thổ cẩm Sapa", 
                "Túi xách thổ cẩm được dệt thủ công bởi người H'Mông Sapa", 
                450000L, "getPlaceholderImageUrl()tho-cam-bag.jpg", thoCamType);
            
            createProduct("Khăn quàng cổ thổ cẩm", 
                "Khăn quàng cổ với họa tiết thổ cẩm truyền thống của người Thái", 
                320000L, "getPlaceholderImageUrl()tho-cam-scarf.jpg", thoCamType);
        }

        // Quần áo lụa
        Type silkType = types.stream().filter(t -> t.getName().equals("Quần áo lụa")).findFirst().orElse(null);
        if (silkType != null) {
            createProduct("Váy lụa thêu hoa", 
                "Váy lụa nữ thêu tay họa tiết hoa cúc, phong cách thanh lịch", 
                1600000L, "getPlaceholderImageUrl()silk-dress.jpg", silkType);
            
            createProduct("Áo sơ mi lụa nam", 
                "Áo sơ mi lụa tự nhiên cho nam giới, thoáng mát và sang trọng", 
                1200000L, "getPlaceholderImageUrl()silk-shirt-men.jpg", silkType);
        }

        // Gốm sứ truyền thống
        Type ceramicType = types.stream().filter(t -> t.getName().equals("Gốm sứ truyền thống")).findFirst().orElse(null);
        if (ceramicType != null) {
            createProduct("Bình hoa gốm Chu Đậu", 
                "Bình hoa gốm Chu Đậu với men xanh ngọc đặc trưng", 
                1200000L, "getPlaceholderImageUrl()chu-dau-vase.jpg", ceramicType);
            
            createProduct("Tách trà gốm Bát Tràng", 
                "Bộ tách trà gốm Bát Tràng họa tiết hoa sen", 
                450000L, "getPlaceholderImageUrl()bat-trang-tea.jpg", ceramicType);
        }

        // Đồ gỗ thủ công
        Type woodType = types.stream().filter(t -> t.getName().equals("Đồ gỗ thủ công")).findFirst().orElse(null);
        if (woodType != null) {
            createProduct("Tượng Phật gỗ mun", 
                "Tượng Phật Di Lặc bằng gỗ mun quý, chạm khắc tinh xảo", 
                2800000L, "getPlaceholderImageUrl()buddha-statue.jpg", woodType);
            
            createProduct("Khay trà gỗ hương", 
                "Khay trà bằng gỗ hương thơm, thiết kế cổ điển", 
                850000L, "getPlaceholderImageUrl()wooden-tray.jpg", woodType);
        }

        // Tranh dân gian
        Type paintingType = types.stream().filter(t -> t.getName().equals("Tranh dân gian")).findFirst().orElse(null);
        if (paintingType != null) {
            createProduct("Tranh Đông Hồ cá chép", 
                "Tranh Đông Hồ truyền thống với họa tiết cá chép hoa sen", 
                320000L, "getPlaceholderImageUrl()dong-ho-fish.jpg", paintingType);
            
            createProduct("Tranh Hàng Trống gà trống", 
                "Tranh Hàng Trống với hình ảnh gà trống báo hiểu", 
                280000L, "getPlaceholderImageUrl()hang-trong-rooster.jpg", paintingType);
        }

        // Tranh sơn mài
        Type lacquerType = types.stream().filter(t -> t.getName().equals("Tranh sơn mài")).findFirst().orElse(null);
        if (lacquerType != null) {
            createProduct("Tranh sơn mài \"Người Gánh Quê\" – 15×15 cm", 
                "Tác phẩm thể hiện hình ảnh người phụ nữ lao động – biểu tượng của sự tần tảo và bền bỉ trong đời sống Việt Nam. Phông nền vàng ấm tượng trưng cho ánh nắng buổi sớm, trong khi mái ngói đỏ và cửa sổ xanh gợi nên khung cảnh yên bình của vùng quê Bắc Bộ.\nCác đường nét đen được vẽ tối giản nhưng vẫn giữ được cảm giác chuyển động tự nhiên của bước chân và nhịp gánh. Bức tranh phù hợp để trưng bày trong không gian mộc mạc, mang đậm hơi thở Việt – như quán cà phê, homestay, hay phòng làm việc.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài truyền thống, thủ công tại làng nghề Hạ Thái – Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng nhũ, xanh lá, nâu đất\n- Phong cách: Tối giản – dân dã – gần gũi\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Hành Trình Bình Dị\" – 15×15 cm", 
                "Bức tranh tái hiện hình ảnh quen thuộc người phụ nữ đội nón lá mặc áo xanh lá, đạp xe chở con nhỏ phía sau. Trên lề đường, một người phụ nữ khác mặc áo hồng đang thong thả đi bộ.\nPhía sau là dãy nhà mái ngói đỏ vàng với hình khối mộc mạc đặc trưng, gợi cảm giác phố cổ Bắc Bộ trong một buổi sáng nhẹ. Gam màu vàng be, đỏ ngói, xám nhạt được phối tinh tế, tạo hiệu ứng mềm mại và tự nhiên.\nTác phẩm mang hơi thở của nhịp sống chậm rãi, thân thiện, khơi gợi ký ức tuổi thơ và tình cảm gia đình giản dị. Phù hợp để treo ở phòng khách, quán cà phê, homestay hoặc làm quà tặng gắn với chủ đề gia đình và ký ức.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Be vàng, đỏ ngói, xanh lá, hồng phấn\n- Phong cách: Hoài cổ – dân dã – ấm áp\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Nắng Trên Phố Cổ\" – 15×15 cm", 
                "Tác phẩm nổi bật với phông nền vàng ánh kim rực rỡ như mặt trời buổi sớm. Những ngôi nhà mái ngói đỏ nâu, tường trắng loang vàng, cùng đường cong mềm dẫn mắt người xem vào chiều sâu không gian.\nĐiểm nhấn của bức tranh là hai dáng người nhỏ – mẹ và con đang cùng nhau đi giữa phố, gợi cảm giác ấm áp, gắn bó gia đình và nét hồn hậu của người Việt.\nGam màu chủ đạo vàng – đỏ – nâu đất hòa quyện tinh tế, thể hiện rõ kỹ thuật sơn mài truyền thống trong cách phối sáng và xử lý bề mặt.\nBức tranh mang lại cảm giác vui tươi, tràn năng lượng và rất phù hợp để trưng bày trong không gian sinh hoạt chung hoặc làm quà tặng mang ý nghĩa \"ấm áp và đoàn viên\".\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công – chế tác tại làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng ánh kim, đỏ ngói, nâu đất\n- Phong cách: Tươi sáng – hoài cổ – mang hơi thở đời thường\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Chuyện Bên Đường\" – 15×15 cm", 
                "Tác phẩm tái hiện lại nét sinh hoạt bình dị của người dân phố cổ – người phụ nữ áo hồng dừng xe đạp, đối diện là người bán hàng rong áo xanh ngồi nghỉ bên đôi quang gánh.\nPhía sau là những ngôi nhà mái ngói đỏ đặc trưng, đường phố uốn cong mềm mại tạo cảm giác gần gũi, mộc mạc.\nGam màu nâu đất, đỏ gạch và xám bạc kết hợp hài hòa, gợi lên không khí yên bình của buổi sáng ở làng nghề Bắc Bộ.\nBức tranh không chỉ mang giá trị thẩm mỹ mà còn khơi gợi cảm xúc về tình làng nghĩa xóm và sự sẻ chia trong đời sống Việt Nam.\nPhù hợp trưng bày trong không gian ấm cúng như phòng khách, quán cà phê hoặc khu trưng bày văn hóa.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài truyền thống – thủ công tại làng nghề Hạ Thái (Thường Tín, Hà Nội)\n- Màu sắc chủ đạo: Nâu đất, đỏ gạch, xám bạc\n- Phong cách: Tối giản – hoài niệm – mộc mạc\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Tiếng Chim Trên Dây Điện\" – 15×15 cm", 
                "Bức tranh thể hiện cột điện và những sợi dây đen căng ngang, nơi ba chú chim nhỏ đậu và một chú đang bay lượn giữa nền trời vàng nhạt. Dưới chân là những mái ngói đỏ đặc trưng, gợi hình ảnh làng cổ miền Bắc.\nTác phẩm đơn giản nhưng tinh tế, phản ánh nhịp sống nhẹ nhàng và bình dị của phố làng Việt Nam. Gam màu vàng ánh kim kết hợp cùng nét vẽ tối giản tạo cảm giác vừa hoài cổ, vừa hiện đại.\nPhù hợp với không gian làm việc, phòng khách hoặc quán cà phê mang phong cách mộc mạc, nghệ thuật.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng ánh kim, đen, đỏ gạch\n- Phong cách: Tối giản – tĩnh lặng – đậm hơi thở Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Người Gánh Hoa Trên Phố\" – 15×15 cm", 
                "Tác phẩm khắc họa hình ảnh quen thuộc của người phụ nữ đội nón lá, mặc áo nâu giản dị, đạp chiếc xe chở hai giỏ hoa tươi nở rộ. Phía sau là dãy nhà mái ngói đỏ cam, tường cũ phai màu, gợi lên khung cảnh của phố cổ Việt Nam trong buổi sớm.\nGam màu cam đất và vàng nhạt chủ đạo tạo nên cảm giác ấm áp, kết hợp cùng ánh bạc nhẹ của lớp sơn mài truyền thống, làm nổi bật vẻ đẹp tinh tế, gần gũi của làng nghề Hạ Thái.\nBức tranh không chỉ là hình ảnh, mà còn là hơi thở của nhịp sống xưa, nơi con người và thiên nhiên cùng hòa quyện trong sự thanh bình.\n\nPhù hợp để trang trí phòng khách, quán cà phê, homestay, hoặc làm món quà lưu niệm mang đậm nét văn hóa Việt.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Cam đất, vàng nhạt, đỏ ngói, nâu, xanh lá\n- Phong cách: Hoài cổ – dân dã – trữ tình\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Người Lái Xích Lô\" – 15×15 cm", 
                "Tác phẩm khắc họa hình ảnh người đàn ông đội nón lá, mặc áo xanh, đang lái xích lô đi dọc theo con đường nhỏ uốn cong. Hai bên là những ngôi nhà mái ngói đỏ nâu, tường sơn vàng bạc, với khối kiến trúc đặc trưng của làng cổ Bắc Bộ.\nBố cục tranh được chia rõ tiền cảnh và hậu cảnh, sử dụng tông be, nâu đất và vàng nhạt làm chủ đạo, tạo cảm giác hài hòa, thanh bình. Các đường viền đen được tiết chế tinh tế, vừa đủ để làm nổi bật hình khối mà vẫn giữ được sự nhẹ nhàng.\nBức tranh mang nét hoài cổ, mộc mạc, gợi lên cảm giác thân quen về một buổi chiều muộn, khi tiếng xe xích lô chậm rãi vọng qua từng con ngõ nhỏ.\nPhù hợp trưng bày tại các không gian mang phong cách vintage, nhà hàng truyền thống hoặc làm quà tặng cho người yêu nghệ thuật Việt.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Be nhạt, đỏ ngói, nâu đất\n- Phong cách: Hoài cổ – giản dị – mang hơi thở Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Con Đường Phố Cũ\" – 15×15 cm", 
                "Bức tranh miêu tả một con phố nhỏ rợp ánh nắng, nơi người lái xích lô đội nón lá đang di chuyển qua khúc cua mềm mại. Nền đường được phủ sắc lam ánh đồng, tạo hiệu ứng phản sáng như mặt đường sau cơn mưa – vừa gần gũi, vừa gợi cảm giác hoài niệm.\nNhững ngôi nhà mái ngói đỏ nâu, tường vàng phấn, cửa gỗ nhỏ là đặc trưng của kiến trúc Bắc Bộ cũ, được thể hiện bằng nét vẽ tối giản nhưng tinh tế.\nToàn bộ bố cục mang hơi hướng sơn mài hiện đại, sử dụng gam màu ấm – lạnh hài hòa, thể hiện sự tĩnh tại và giản dị trong đời sống thường nhật.\nBức tranh phù hợp để treo ở không gian mang phong cách mộc, vintage hoặc làm quà tặng văn hóa mang dấu ấn Việt Nam.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công – chế tác tại làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Lam ánh đồng, đỏ ngói, vàng đất\n- Phong cách: Hoài cổ – dân dã – đậm bản sắc Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Phố Xưa Yên Bình\" – 15×15 cm", 
                "Bức tranh khắc họa người đội nón lá ngồi trên chiếc xích lô, lặng lẽ di chuyển qua dãy nhà mái ngói đỏ đặc trưng. Tông nền vàng nhạt làm nổi bật vẻ ấm áp và hoài niệm, gợi nhớ những buổi chiều nắng nhẹ trên phố cổ.\nHình ảnh giản dị ấy phản chiếu nhịp sống chậm rãi, thân quen của người Việt, nơi con người và phố phường hòa làm một.\nTừng đường nét được vẽ thủ công bằng kỹ thuật sơn mài truyền thống Hạ Thái, mang lại độ bóng mờ đặc trưng và chiều sâu tinh tế.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng nhạt, đỏ gạch, be, nâu đất\n- Phong cách: Hoài cổ – mộc mạc – yên bình\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Tháp Rùa – Hồ Gươm trong sắc thu\"", 
                "Bức tranh khắc họa Tháp Rùa soi bóng trên mặt nước Hồ Gươm – hình ảnh gắn liền với tâm thức người Việt. Những tán lộc vừng đỏ rực vươn mình trên nền trời vàng sậm, như một nét chấm phá của mùa thu Hà Nội.\nBề mặt tranh phủ ánh vàng nhẹ, tạo cảm giác lung linh như sương sớm trên mặt hồ.\n- Chất liệu: Sơn mài truyền thống, vẽ tay hoàn toàn, phủ bạc và đánh bóng nhiều lớp.\n- Ý nghĩa: Tượng trưng cho sự tĩnh tại, thanh bình và niềm tự hào ngàn năm văn hiến.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Trang trí phòng khách, quán cà phê, homestay hoặc làm quà tặng du lịch.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Chùa Một Cột – Đóa Sen giữa lòng Thủ đô\"", 
                "Bức tranh tái hiện Chùa Một Cột – công trình kiến trúc độc đáo của Việt Nam, được ví như bông sen nở trên mặt nước.\nTông nền vàng đồng pha ánh nâu đất gợi cảm giác cổ kính, hài hòa với sắc đỏ của mái chùa và mảng xanh của cây cối.\nTừng đường nét mềm mại thể hiện sự tinh tế trong kỹ thuật sơn mài, tạo chiều sâu và độ bóng đặc trưng.\n- Ý nghĩa: Biểu trưng cho tinh thần hiếu đạo, sự thanh tịnh và sức sống trường tồn của văn hóa Việt.\n- Chất liệu: Sơn ta, bạc quỳ, vỏ trứng.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Làm quà lưu niệm cho du khách quốc tế hoặc trưng bày tại không gian thiền, spa, khách sạn.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Văn Miếu – Quốc Tử Giám\"", 
                "Tác phẩm mô phỏng Khuê Văn Các – biểu tượng của Văn Miếu Quốc Tử Giám, hòa cùng mảng xanh của cây cổ thụ.\nBức tường gạch đỏ và mái ngói cong uốn lượn tạo nên cảm giác trang nghiêm nhưng vẫn gần gũi.\nBề mặt sơn mài phủ vàng ánh, phản chiếu ánh sáng nhẹ, mang lại chiều sâu cổ kính đặc trưng của kiến trúc thời Lý.\n- Ý nghĩa: Thể hiện truyền thống tôn sư trọng đạo, đề cao tri thức và lòng tự hào dân tộc.\n- Chất liệu: Sơn ta, bạc quỳ, vỏ trứng, phủ bóng thủ công.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Làm quà tặng cho học sinh, sinh viên, thầy cô, hoặc trang trí phòng làm việc.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
        }

        log.info("Seeded {} products", productRepository.count());
    }

    private void createProduct(String name, String description, Long price, String pictureUrl, Type type) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .pictureUrl(pictureUrl)
                .type(type)
                .status(Product.ProductStatus.ACTIVE) // Default to ACTIVE
                .quantity(10) // Default quantity
                .build();
        productRepository.save(product);
    }

    /**
     * Get placeholder image URL for products
     */
    private String getPlaceholderImageUrl() {
        return "/uploads/products/e0c1330c-45e5-49cc-b4d1-f19dc4589329.jpg";
    }

    private void seedUsers() {
        log.info("Seeding users...");
        
        // Admin user
        User admin = User.builder()
                .username("admin")
                .email("admin@sonmai.com")
                .password(passwordEncoder.encode("123456"))
                .firstName("Admin")
                .lastName("System")
                .phone("0123456789")
                .role(User.Role.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        userRepository.save(admin);

        // Customer users
        List<User> customers = Arrays.asList(
            createUser("customer1", "nguyen.van.a@gmail.com", "Nguyễn", "Văn A", "0987654321"),
            createUser("customer2", "tran.thi.b@gmail.com", "Trần", "Thị B", "0976543210"),
            createUser("customer3", "le.van.c@gmail.com", "Lê", "Văn C", "0965432109"),
            createUser("customer4", "pham.thi.d@gmail.com", "Phạm", "Thị D", "0954321098")
        );

        userRepository.saveAll(customers);
        log.info("Seeded {} users", userRepository.count());
    }

    private User createUser(String username, String email, String firstName, String lastName, String phone) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("123456"))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .role(User.Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    private void seedAddresses() {
        log.info("Seeding addresses...");
        
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (!user.getRole().equals(User.Role.ADMIN)) {
                Address address = Address.builder()
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .phone(user.getPhone())
                        .street("123 Đường " + user.getFirstName())
                        .ward("Phường Trung Tâm")
                        .district("Quận 1")
                        .province("TP. Hồ Chí Minh")
                        .user(user)
                        .build();
                addressRepository.save(address);
            }
        }
        
        log.info("Seeded {} addresses", addressRepository.count());
    }
    
    public void forceRecreateData() {
        log.info("Force recreating all data...");
        
        try {
            // Delete all data in correct order to avoid foreign key constraints
            orderRepository.deleteAll();
            productRepository.deleteAll();
            typeRepository.deleteAll();
            addressRepository.deleteAll();
            userRepository.deleteAll();
            
            // Recreate all data
            seedProductTypes();
            seedProducts();
            seedUsers();
            seedAddresses();
            
            log.info("Data recreated successfully!");
        } catch (Exception e) {
            log.error("Error recreating data: {}", e.getMessage());
            throw new RuntimeException("Failed to recreate data: " + e.getMessage());
        }
    }
    
    public void seedNewProductData() {
        log.info("Seeding new product data...");
        
        try {
            // Add some new products to existing types
            List<Type> types = typeRepository.findAll();
            
            if (!types.isEmpty()) {
                Type randomType = types.get(0);
                createProduct("Sản phẩm mới 1", "Mô tả sản phẩm mới", 500000L, "getPlaceholderImageUrl()new-product-1.jpg", randomType);
                createProduct("Sản phẩm mới 2", "Mô tả sản phẩm mới", 750000L, "getPlaceholderImageUrl()new-product-2.jpg", randomType);
            }
            
            log.info("New product data seeded successfully!");
        } catch (Exception e) {
            log.error("Error seeding new product data: {}", e.getMessage());
            throw new RuntimeException("Failed to seed new product data: " + e.getMessage());
        }
    }
    
    public void seedSampleOrders() {
        log.info("Seeding sample orders...");
        
        try {
            List<User> customers = userRepository.findAll().stream()
                    .filter(user -> user.getRole().equals(User.Role.USER))
                    .toList();
            
            List<Product> products = productRepository.findAll();
            
            if (!customers.isEmpty() && !products.isEmpty()) {
                // Create sample order for first customer
                User customer = customers.get(0);
                Product product = products.get(0);
                
                String orderCode = "ORD" + System.currentTimeMillis();
                
                Order order = Order.builder()
                        .orderCode(orderCode)
                        .user(customer)
                        .totalAmount(320000L)
                        .status(Order.OrderStatus.CONFIRMED)
                        .shipFullName("Nguyễn Văn A")
                        .shipPhone("0123456789")
                        .shipStreet("123 Đường ABC")
                        .shipWard("Phường XYZ")
                        .shipDistrict("Quận 1")
                        .shipProvince("TP.HCM")
                        .createdDate(LocalDateTime.now().minusDays(1))
                        .updatedDate(LocalDateTime.now().minusDays(1))
                        .build();
                
                orderRepository.save(order);
                log.info("Created sample order: {}", orderCode);
            }
            
            log.info("Sample orders seeded successfully!");
        } catch (Exception e) {
            log.error("Error seeding sample orders: {}", e.getMessage());
            throw new RuntimeException("Failed to seed sample orders: " + e.getMessage());
        }
    }
    
    public long getProductCount() {
        return productRepository.count();
    }
    
    public long getTypeCount() {
        return typeRepository.count();
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
    
    public long getOrderCount() {
        return orderRepository.count();
    }
    
    public void seedProductsSimple() {
        log.info("Seeding products...");
        
        try {
            List<Type> types = typeRepository.findAll();
            
            if (types.isEmpty()) {
                log.warn("No types found, seeding types first");
                seedProductTypes();
                types = typeRepository.findAll();
            }
            
            // Create some sample products
            if (!types.isEmpty()) {
                Type firstType = types.get(0);
                
                // Create multiple products
                for (int i = 1; i <= 10; i++) {
                    createProduct(
                        "Sản phẩm mẫu " + i,
                        "Mô tả sản phẩm mẫu " + i,
                        100000L * i,
                        "getPlaceholderImageUrl()product-" + i + ".jpg",
                        firstType
                    );
                }
                
                log.info("Created 10 sample products");
            }
            
        } catch (Exception e) {
            log.error("Error seeding products: {}", e.getMessage());
            throw new RuntimeException("Failed to seed products: " + e.getMessage());
        }
    }
    
    public void addLacquerPaintings() {
        log.info("Adding lacquer paintings to database...");
        
        try {
            // Tìm hoặc tạo loại "Tranh sơn mài"
            Type lacquerType = typeRepository.findByName("Tranh sơn mài").orElse(null);
            if (lacquerType == null) {
                lacquerType = Type.builder()
                        .name("Tranh sơn mài")
                        .build();
                typeRepository.save(lacquerType);
                log.info("Created new product type: Tranh sơn mài");
            }
            
            // Thêm các sản phẩm tranh sơn mài
            createProduct("Tranh sơn mài \"Người Gánh Quê\" – 15×15 cm", 
                "Tác phẩm thể hiện hình ảnh người phụ nữ lao động – biểu tượng của sự tần tảo và bền bỉ trong đời sống Việt Nam. Phông nền vàng ấm tượng trưng cho ánh nắng buổi sớm, trong khi mái ngói đỏ và cửa sổ xanh gợi nên khung cảnh yên bình của vùng quê Bắc Bộ.\nCác đường nét đen được vẽ tối giản nhưng vẫn giữ được cảm giác chuyển động tự nhiên của bước chân và nhịp gánh. Bức tranh phù hợp để trưng bày trong không gian mộc mạc, mang đậm hơi thở Việt – như quán cà phê, homestay, hay phòng làm việc.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài truyền thống, thủ công tại làng nghề Hạ Thái – Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng nhũ, xanh lá, nâu đất\n- Phong cách: Tối giản – dân dã – gần gũi\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Hành Trình Bình Dị\" – 15×15 cm", 
                "Bức tranh tái hiện hình ảnh quen thuộc người phụ nữ đội nón lá mặc áo xanh lá, đạp xe chở con nhỏ phía sau. Trên lề đường, một người phụ nữ khác mặc áo hồng đang thong thả đi bộ.\nPhía sau là dãy nhà mái ngói đỏ vàng với hình khối mộc mạc đặc trưng, gợi cảm giác phố cổ Bắc Bộ trong một buổi sáng nhẹ. Gam màu vàng be, đỏ ngói, xám nhạt được phối tinh tế, tạo hiệu ứng mềm mại và tự nhiên.\nTác phẩm mang hơi thở của nhịp sống chậm rãi, thân thiện, khơi gợi ký ức tuổi thơ và tình cảm gia đình giản dị. Phù hợp để treo ở phòng khách, quán cà phê, homestay hoặc làm quà tặng gắn với chủ đề gia đình và ký ức.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Be vàng, đỏ ngói, xanh lá, hồng phấn\n- Phong cách: Hoài cổ – dân dã – ấm áp\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Nắng Trên Phố Cổ\" – 15×15 cm", 
                "Tác phẩm nổi bật với phông nền vàng ánh kim rực rỡ như mặt trời buổi sớm. Những ngôi nhà mái ngói đỏ nâu, tường trắng loang vàng, cùng đường cong mềm dẫn mắt người xem vào chiều sâu không gian.\nĐiểm nhấn của bức tranh là hai dáng người nhỏ – mẹ và con đang cùng nhau đi giữa phố, gợi cảm giác ấm áp, gắn bó gia đình và nét hồn hậu của người Việt.\nGam màu chủ đạo vàng – đỏ – nâu đất hòa quyện tinh tế, thể hiện rõ kỹ thuật sơn mài truyền thống trong cách phối sáng và xử lý bề mặt.\nBức tranh mang lại cảm giác vui tươi, tràn năng lượng và rất phù hợp để trưng bày trong không gian sinh hoạt chung hoặc làm quà tặng mang ý nghĩa \"ấm áp và đoàn viên\".\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công – chế tác tại làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng ánh kim, đỏ ngói, nâu đất\n- Phong cách: Tươi sáng – hoài cổ – mang hơi thở đời thường\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Chuyện Bên Đường\" – 15×15 cm", 
                "Tác phẩm tái hiện lại nét sinh hoạt bình dị của người dân phố cổ – người phụ nữ áo hồng dừng xe đạp, đối diện là người bán hàng rong áo xanh ngồi nghỉ bên đôi quang gánh.\nPhía sau là những ngôi nhà mái ngói đỏ đặc trưng, đường phố uốn cong mềm mại tạo cảm giác gần gũi, mộc mạc.\nGam màu nâu đất, đỏ gạch và xám bạc kết hợp hài hòa, gợi lên không khí yên bình của buổi sáng ở làng nghề Bắc Bộ.\nBức tranh không chỉ mang giá trị thẩm mỹ mà còn khơi gợi cảm xúc về tình làng nghĩa xóm và sự sẻ chia trong đời sống Việt Nam.\nPhù hợp trưng bày trong không gian ấm cúng như phòng khách, quán cà phê hoặc khu trưng bày văn hóa.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài truyền thống – thủ công tại làng nghề Hạ Thái (Thường Tín, Hà Nội)\n- Màu sắc chủ đạo: Nâu đất, đỏ gạch, xám bạc\n- Phong cách: Tối giản – hoài niệm – mộc mạc\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Tiếng Chim Trên Dây Điện\" – 15×15 cm", 
                "Bức tranh thể hiện cột điện và những sợi dây đen căng ngang, nơi ba chú chim nhỏ đậu và một chú đang bay lượn giữa nền trời vàng nhạt. Dưới chân là những mái ngói đỏ đặc trưng, gợi hình ảnh làng cổ miền Bắc.\nTác phẩm đơn giản nhưng tinh tế, phản ánh nhịp sống nhẹ nhàng và bình dị của phố làng Việt Nam. Gam màu vàng ánh kim kết hợp cùng nét vẽ tối giản tạo cảm giác vừa hoài cổ, vừa hiện đại.\nPhù hợp với không gian làm việc, phòng khách hoặc quán cà phê mang phong cách mộc mạc, nghệ thuật.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng ánh kim, đen, đỏ gạch\n- Phong cách: Tối giản – tĩnh lặng – đậm hơi thở Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Người Gánh Hoa Trên Phố\" – 15×15 cm", 
                "Tác phẩm khắc họa hình ảnh quen thuộc của người phụ nữ đội nón lá, mặc áo nâu giản dị, đạp chiếc xe chở hai giỏ hoa tươi nở rộ. Phía sau là dãy nhà mái ngói đỏ cam, tường cũ phai màu, gợi lên khung cảnh của phố cổ Việt Nam trong buổi sớm.\nGam màu cam đất và vàng nhạt chủ đạo tạo nên cảm giác ấm áp, kết hợp cùng ánh bạc nhẹ của lớp sơn mài truyền thống, làm nổi bật vẻ đẹp tinh tế, gần gũi của làng nghề Hạ Thái.\nBức tranh không chỉ là hình ảnh, mà còn là hơi thở của nhịp sống xưa, nơi con người và thiên nhiên cùng hòa quyện trong sự thanh bình.\n\nPhù hợp để trang trí phòng khách, quán cà phê, homestay, hoặc làm món quà lưu niệm mang đậm nét văn hóa Việt.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Cam đất, vàng nhạt, đỏ ngói, nâu, xanh lá\n- Phong cách: Hoài cổ – dân dã – trữ tình\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Người Lái Xích Lô\" – 15×15 cm", 
                "Tác phẩm khắc họa hình ảnh người đàn ông đội nón lá, mặc áo xanh, đang lái xích lô đi dọc theo con đường nhỏ uốn cong. Hai bên là những ngôi nhà mái ngói đỏ nâu, tường sơn vàng bạc, với khối kiến trúc đặc trưng của làng cổ Bắc Bộ.\nBố cục tranh được chia rõ tiền cảnh và hậu cảnh, sử dụng tông be, nâu đất và vàng nhạt làm chủ đạo, tạo cảm giác hài hòa, thanh bình. Các đường viền đen được tiết chế tinh tế, vừa đủ để làm nổi bật hình khối mà vẫn giữ được sự nhẹ nhàng.\nBức tranh mang nét hoài cổ, mộc mạc, gợi lên cảm giác thân quen về một buổi chiều muộn, khi tiếng xe xích lô chậm rãi vọng qua từng con ngõ nhỏ.\nPhù hợp trưng bày tại các không gian mang phong cách vintage, nhà hàng truyền thống hoặc làm quà tặng cho người yêu nghệ thuật Việt.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Be nhạt, đỏ ngói, nâu đất\n- Phong cách: Hoài cổ – giản dị – mang hơi thở Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng gói trong hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Con Đường Phố Cũ\" – 15×15 cm", 
                "Bức tranh miêu tả một con phố nhỏ rợp ánh nắng, nơi người lái xích lô đội nón lá đang di chuyển qua khúc cua mềm mại. Nền đường được phủ sắc lam ánh đồng, tạo hiệu ứng phản sáng như mặt đường sau cơn mưa – vừa gần gũi, vừa gợi cảm giác hoài niệm.\nNhững ngôi nhà mái ngói đỏ nâu, tường vàng phấn, cửa gỗ nhỏ là đặc trưng của kiến trúc Bắc Bộ cũ, được thể hiện bằng nét vẽ tối giản nhưng tinh tế.\nToàn bộ bố cục mang hơi hướng sơn mài hiện đại, sử dụng gam màu ấm – lạnh hài hòa, thể hiện sự tĩnh tại và giản dị trong đời sống thường nhật.\nBức tranh phù hợp để treo ở không gian mang phong cách mộc, vintage hoặc làm quà tặng văn hóa mang dấu ấn Việt Nam.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công – chế tác tại làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Lam ánh đồng, đỏ ngói, vàng đất\n- Phong cách: Hoài cổ – dân dã – đậm bản sắc Việt\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh sơn mài \"Phố Xưa Yên Bình\" – 15×15 cm", 
                "Bức tranh khắc họa người đội nón lá ngồi trên chiếc xích lô, lặng lẽ di chuyển qua dãy nhà mái ngói đỏ đặc trưng. Tông nền vàng nhạt làm nổi bật vẻ ấm áp và hoài niệm, gợi nhớ những buổi chiều nắng nhẹ trên phố cổ.\nHình ảnh giản dị ấy phản chiếu nhịp sống chậm rãi, thân quen của người Việt, nơi con người và phố phường hòa làm một.\nTừng đường nét được vẽ thủ công bằng kỹ thuật sơn mài truyền thống Hạ Thái, mang lại độ bóng mờ đặc trưng và chiều sâu tinh tế.\n\nThông tin sản phẩm:\n- Kích thước: 15 × 15 cm\n- Chất liệu: Sơn mài thủ công truyền thống – làng nghề Hạ Thái, Thường Tín, Hà Nội\n- Màu sắc chủ đạo: Vàng nhạt, đỏ gạch, be, nâu đất\n- Phong cách: Hoài cổ – mộc mạc – yên bình\n\nBảo hành & đóng gói:\n- Bảo hành lớp sơn 6 tháng\n- Đóng hộp kraft in logo Sơn Mài Heritage", 
                250000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Tháp Rùa – Hồ Gươm trong sắc thu\"", 
                "Bức tranh khắc họa Tháp Rùa soi bóng trên mặt nước Hồ Gươm – hình ảnh gắn liền với tâm thức người Việt. Những tán lộc vừng đỏ rực vươn mình trên nền trời vàng sậm, như một nét chấm phá của mùa thu Hà Nội.\nBề mặt tranh phủ ánh vàng nhẹ, tạo cảm giác lung linh như sương sớm trên mặt hồ.\n- Chất liệu: Sơn mài truyền thống, vẽ tay hoàn toàn, phủ bạc và đánh bóng nhiều lớp.\n- Ý nghĩa: Tượng trưng cho sự tĩnh tại, thanh bình và niềm tự hào ngàn năm văn hiến.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Trang trí phòng khách, quán cà phê, homestay hoặc làm quà tặng du lịch.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Chùa Một Cột – Đóa Sen giữa lòng Thủ đô\"", 
                "Bức tranh tái hiện Chùa Một Cột – công trình kiến trúc độc đáo của Việt Nam, được ví như bông sen nở trên mặt nước.\nTông nền vàng đồng pha ánh nâu đất gợi cảm giác cổ kính, hài hòa với sắc đỏ của mái chùa và mảng xanh của cây cối.\nTừng đường nét mềm mại thể hiện sự tinh tế trong kỹ thuật sơn mài, tạo chiều sâu và độ bóng đặc trưng.\n- Ý nghĩa: Biểu trưng cho tinh thần hiếu đạo, sự thanh tịnh và sức sống trường tồn của văn hóa Việt.\n- Chất liệu: Sơn ta, bạc quỳ, vỏ trứng.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Làm quà lưu niệm cho du khách quốc tế hoặc trưng bày tại không gian thiền, spa, khách sạn.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
            
            createProduct("Tranh \"Văn Miếu – Quốc Tử Giám\"", 
                "Tác phẩm mô phỏng Khuê Văn Các – biểu tượng của Văn Miếu Quốc Tử Giám, hòa cùng mảng xanh của cây cổ thụ.\nBức tường gạch đỏ và mái ngói cong uốn lượn tạo nên cảm giác trang nghiêm nhưng vẫn gần gũi.\nBề mặt sơn mài phủ vàng ánh, phản chiếu ánh sáng nhẹ, mang lại chiều sâu cổ kính đặc trưng của kiến trúc thời Lý.\n- Ý nghĩa: Thể hiện truyền thống tôn sư trọng đạo, đề cao tri thức và lòng tự hào dân tộc.\n- Chất liệu: Sơn ta, bạc quỳ, vỏ trứng, phủ bóng thủ công.\n- Kích thước: 25 x 25 cm\n- Phù hợp: Làm quà tặng cho học sinh, sinh viên, thầy cô, hoặc trang trí phòng làm việc.", 
                350000L, getPlaceholderImageUrl(), lacquerType);
            
            log.info("Successfully added {} lacquer paintings to database", 12);
            
        } catch (Exception e) {
            log.error("Error adding lacquer paintings: {}", e.getMessage());
            throw new RuntimeException("Failed to add lacquer paintings: " + e.getMessage());
        }
    }
}

