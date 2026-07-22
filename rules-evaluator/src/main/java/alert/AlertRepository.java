package alert;

import java.util.List;

public interface AlertRepository {

    void save(Alert alert);

    List<Alert> findAllOrderByCreatedAt();
}