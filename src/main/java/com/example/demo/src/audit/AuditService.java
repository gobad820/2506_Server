package com.example.demo.src.audit;

import com.example.demo.src.audit.dto.UserAuditDto;
import com.example.demo.src.user.entity.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<UserAuditDto> getUserAudit(Long userId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        AuditQuery query = reader.createQuery()
            .forRevisionsOfEntity(User.class, false, true)
            .add(AuditEntity.id().eq(userId))
            .addOrder(AuditEntity.revisionNumber().asc());
        List<Object[]> resultList = query.getResultList();
        ArrayList<UserAuditDto> userAuditDtos = new ArrayList<>();

        for (Object[] result : resultList) {
            User user = (User) result[0];
            DefaultRevisionEntity revEntity = (DefaultRevisionEntity) result[1];
            RevisionType revType = (RevisionType) result[2];

            LocalDateTime revDateTime = new Date(revEntity.getTimestamp()).toInstant()
                .atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

            userAuditDtos.add(
                UserAuditDto.from(user, (long) revEntity.getId(), revDateTime, revType));
        }

        return userAuditDtos;
    }
}
