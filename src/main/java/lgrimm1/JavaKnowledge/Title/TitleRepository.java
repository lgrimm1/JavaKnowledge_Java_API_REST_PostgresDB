package lgrimm1.JavaKnowledge.Title;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface TitleRepository extends JpaRepository<TitleEntity, Long> {

	Optional<TitleEntity> findByTitle(String title);

	List<TitleEntity> findByTitleContainingAllIgnoreCase(String titlePart);

	Optional<TitleEntity> findByFilename(String filename);

	Optional<TitleEntity> findByTxtId(long txtId);
}
