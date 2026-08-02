package com.unidata.uni.dto;

import com.unidata.uni.entity.ScoreLine;

import java.time.LocalDateTime;

/**
 * 分数线视图：非会员查看时隐藏具体分数，仅提示数据为会员专享。
 */
public record ScoreLineView(
        Long id,
        Long schoolId,
        String schoolName,
        Integer year,
        String major,
        String lineType,
        Integer minScore,
        Integer politicalScore,
        Integer foreignScore,
        Integer majorScore1,
        Integer majorScore2,
        String remark,
        Boolean premium,
        Boolean locked,
        LocalDateTime createdAt) {

    public static ScoreLineView from(ScoreLine s, boolean canViewPremium) {
        boolean locked = Boolean.TRUE.equals(s.getPremium()) && !canViewPremium;
        return new ScoreLineView(
                s.getId(), s.getSchoolId(), s.getSchoolName(), s.getYear(), s.getMajor(),
                s.getLineType(),
                locked ? null : s.getMinScore(),
                locked ? null : s.getPoliticalScore(),
                locked ? null : s.getForeignScore(),
                locked ? null : s.getMajorScore1(),
                locked ? null : s.getMajorScore2(),
                s.getRemark(),
                s.getPremium(),
                locked,
                s.getCreatedAt());
    }
}
