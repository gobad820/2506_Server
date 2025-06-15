package com.example.demo.src.audit;

import static com.example.demo.common.Constant.KOREA_ZONE;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.audit.model.UserAuditReq;
import com.example.demo.src.audit.model.UserAuditRes;
import com.example.demo.src.user.entity.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {


    @PersistenceContext
    private EntityManager entityManager;

    private final AuditDataManager auditDataManager;

    /**
     * 특정 사용자의 감사 기록 조회
     *
     * @param userId
     * @return 해당 사용자의 감사 기록
     * @throws BaseException 감사 시스템 오류 시
     */
    public List<UserAuditRes> getUserAudit(Long userId) {
        try {
            User user = auditDataManager.getUserByIdAndState(userId,
                    State.ACTIVE)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
            AuditReader reader = AuditReaderFactory.get(entityManager);
            AuditQuery query = reader.createQuery()
                .forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.id().eq(user.getId()))
                .addOrder(AuditEntity.revisionNumber().asc());
            @SuppressWarnings("unchecked")
            List<Object[]> resultList = query.getResultList();
            ArrayList<UserAuditRes> userAuditRes = new ArrayList<>();
            for (Object[] result : resultList) {
                try {
                    UserAuditRes auditRes = convertToUserAuditDto(result);
                    userAuditRes.add(auditRes);
                } catch (Exception e) {
                    log.error("Error converting audit record for user id: {}", userId);
                }
            }
            return userAuditRes;
        } catch (AuditException e) {
            log.error("Hibernate Envers audit error for user Id: {}", userId, e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error retrieving audit for user Id: {}", userId, e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        }
    }

    /**
     * 시스템 전체 감사 기록 조회 - 페이지네이션
     *
     * @param request
     * @param pageable
     * @return 페이징된 기록
     * @throws BaseException 감사 시스템 오류 시
     */
    public Page<UserAuditRes> getSystemAuditHistory(UserAuditReq request,
        Pageable pageable) {
        try {
            log.info("Request: {}", request);

            AuditReader reader = AuditReaderFactory.get(entityManager);
            AuditQuery query = reader.createQuery()
                .forRevisionsOfEntity(User.class, false, true);

            query = addDateRangeFilter(query, request.getStartDateTime(), request.getEndDateTime());
            query = addTargetUserFilter(query, request.getTargetUserId());
            query = addRevisionTypeFilter(query, request.getRevisionType());

            long total = getTotalCount(request);
            log.info("Total Request Number {}", total);
            query = addSorting(query, pageable);
            query = addPagination(query, pageable);

            List<Object[]> results = query.getResultList();
            List<UserAuditRes> auditInfos = results.stream().map(this::convertToUserAuditDto)
                .collect(Collectors.toList());

            return new PageImpl<>(auditInfos, pageable, total);
        } catch (AuditException e) {
            log.error("Hibernate Envers audit error for system history", e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error retrieving system audit history", e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        }
    }


    /**
     * 특정 리비전 디테일 조회
     *
     * @param revisionId
     * @return revision detail
     * @throws BaseException 존재하지 않는 리비전이거나 시스템 오류 발생 시
     */
    public UserAuditRes getRevisionDetail(Long revisionId) {
        try {
            log.debug("Retrieving revision detail for revision ID:{}", revisionId);
            AuditReader reader = AuditReaderFactory.get(entityManager);
            AuditQuery query = reader.createQuery().forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.revisionNumber().eq(revisionId.intValue()));

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                log.warn("No Revision found for revision ID: {}", revisionId);
                throw new BaseException(BaseResponseStatus.AUDIT_DATA_NOT_FOUND);
            }

            UserAuditRes result = convertToUserAuditDto(results.get(0));
            log.debug("revision ID {} 조회 성공", revisionId);
            return result;
        } catch (BaseException e) {
            throw e;
        } catch (AuditException e) {
            log.error("Hibernate Envers audit error for revision Id: {}", revisionId, e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error retrieving audit for revision Id: {}", revisionId, e);
            throw new BaseException(BaseResponseStatus.AUDIT_SYSTEM_ERROR);
        }
    }

    private AuditQuery addSorting(AuditQuery query, Pageable pageable) {
        query.addOrder(AuditEntity.revisionProperty("timestamp").desc());
        return query;
    }

    private long getTotalCount(UserAuditReq reqeust) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        AuditQuery countQuery = reader.createQuery().forRevisionsOfEntity(User.class, false, true);

        countQuery = addDateRangeFilter(countQuery, reqeust.getStartDateTime(),
            reqeust.getEndDateTime());
        countQuery = addTargetUserFilter(countQuery, reqeust.getTargetUserId());

        return countQuery.getResultList().size();
    }

    private UserAuditRes convertToUserAuditDto(Object[] results) {
        User userRevision = (User) results[0];
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) results[1];
        RevisionType revisionType = (RevisionType) results[2];

        LocalDateTime revDateTime = new Date(revisionEntity.getTimestamp()).toInstant()
            .atZone(KOREA_ZONE).toLocalDateTime();

        return UserAuditRes.from(userRevision, (long) revisionEntity.getId(), revDateTime,
            revisionType);
    }

    private AuditQuery addDateRangeFilter(AuditQuery query, LocalDateTime startDate,
        LocalDateTime endDate) {
        AuditProperty<Object> timestamp = AuditEntity.revisionProperty("timestamp");
        if (startDate != null) {
            long startTimeStamp = startDate.atZone(KOREA_ZONE).toInstant().toEpochMilli();
            query.add(timestamp.ge(startTimeStamp));
        }

        if (endDate != null) {
            long endTimeStamp = endDate.atZone(KOREA_ZONE).toInstant().toEpochMilli();
            query.add(timestamp.le(endTimeStamp));
        }

        return query;
    }


    private AuditQuery addTargetUserFilter(AuditQuery query, Long targetUserId) {
        if (targetUserId != null) {
            query.add(AuditEntity.id().eq(targetUserId));
        }
        return query;
    }

    private AuditQuery addRevisionTypeFilter(AuditQuery query, RevisionType revisionType) {
        if (revisionType != null) {
            query.add(AuditEntity.revisionType().eq(revisionType));
        }
        return query;
    }

    private AuditQuery addPagination(AuditQuery query, Pageable pageable) {
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query;
    }


}
