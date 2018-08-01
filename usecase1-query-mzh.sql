WITH S1 as
(
select rownum, col3_hr_min, col2, col7, col15, col16,truncate(col3_hr_min, -2) as col3_hr_min_msec
	from SWAP_MST_SHARD_PT_HR_MIN
where
col2 = 'USD/JPY'
and
col7 = 'TKYMIZ.Edo'
and
col26 = 'A'
and
col27 = 'A'
and
col12 = 0
),
stg1 as (
select
		col3_hr_min,
		col15,
		col16,
		first_value(col15) over (partition by col3_hr_min_msec order by col3_hr_min) as fir15,
		last_value(col15) over (partition by col3_hr_min_msec order by col3_hr_min ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as las15,
		first_value(col16) over (partition by col3_hr_min_msec order by col3_hr_min) as fir16,
		last_value(col16) over (partition by col3_hr_min_msec order by col3_hr_min ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as las16,
		col3_hr_min_msec
		from 
			S1
group by col3_hr_min, col15, col16, col3_hr_min_msec
),
stg2 as (
select
	avg(col15) AS AVG_COLUM15,
	max(col15) as MAX_COLUM15,
	min(col15) as MIN_COLUM15,
	avg(col16) AS AVG_COLUM16,
	max(col16) as MAX_COLUM16,
	min(col16) as MIN_COLUM16,
	col3_hr_min_msec,
	ki_shard_key(col3_hr_min_msec)
from stg1
group by fir15, las15, fir16, las16, col3_hr_min_msec
),
ntile_o as (
select 
col15,col3_hr_min_msec,ntile(2) over (partition by col3_hr_min_msec order by col15) as ntile2_15,
col16,ntile(2) over (partition by col3_hr_min_msec order by col16) as ntile2_16
from S1
),
ntile_o_1 as (
select col3_hr_min_msec, count(*) as cnt,
MAX((CASE WHEN ntile2_15=1 THEN col15 END)) AS max_ntile2_15_1,
MIN((CASE WHEN ntile2_15=2 THEN col15 END)) AS min_ntile2_15_2,
MAX((CASE WHEN ntile2_16=1 THEN col16 END)) AS max_ntile2_16_1,
MIN((CASE WHEN ntile2_16=2 THEN col16 END)) AS min_ntile2_16_2
from ntile_o
group by col3_hr_min_msec
),
ntile_o_2 as (
select col3_hr_min_msec as col3_hr_min_msec_1, 
(CASE mod(cnt,2)
    WHEN 1 THEN max_ntile2_15_1
    ELSE (max_ntile2_15_1+min_ntile2_15_2)/2.0 END) AS median_col15,
    (CASE mod(cnt,2)
    WHEN 1 THEN max_ntile2_16_1
    ELSE (max_ntile2_16_1+min_ntile2_16_2)/2.0 END) AS median_col16,
    ki_shard_key(col3_hr_min_msec)
from ntile_o_1
)
select col3_hr_min_msec, AVG_COLUM15, MAX_COLUM15, MIN_COLUM15, median_col15, AVG_COLUM16, MAX_COLUM16, MIN_COLUM16, median_col16
from stg2, ntile_o_2
where col3_hr_min_msec = col3_hr_min_msec_1