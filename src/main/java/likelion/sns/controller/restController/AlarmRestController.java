package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.alarm.AlarmDeleteResponseDto;
import likelion.sns.domain.dto.alarm.AlarmListDto;
import likelion.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.sql.SQLException;

@RestController
@RequestMapping("api/v1/alarms")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"Alarm API"})
public class AlarmRestController {

    private final AlarmService alarmService;


    /**
     * 요청자에게 온 알림 페이징 조회(최신순으로 정렬)
     */
    @GetMapping
    @ApiOperation(value = "Alarm 리스트 조회", notes = "발생된 알림을 최신순으로 20개씩 페이징 해서 가져온다.")
    public Response showAlarms(@ApiIgnore Authentication authentication,@ApiIgnore Pageable pageable) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("알림 조회 요청자 userName : {}", requestUserName);

        Page<AlarmListDto> alarms = alarmService.getAlarms(requestUserName, pageable);

        return Response.success(alarms);
    }

    /**
     * 요청자에게 온 알림 삭제
     */
    @DeleteMapping("/{alarmId}")
    @ApiOperation(value = "Alarm 리스트 조회", notes = "발생된 알림을 최신순으로 20개씩 페이징 해서 가져온다.")
    public Response deleteOneAlarm(@PathVariable(name = "alarmId") Long alarmId, @ApiIgnore Authentication authentication, @ApiIgnore Pageable pageable) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("알림 삭제 요청자 userName : {}", requestUserName);

        alarmService.deleteAlarm(requestUserName, alarmId);

        AlarmDeleteResponseDto responseDto = new AlarmDeleteResponseDto(alarmId);
        return Response.success(responseDto);
    }
}
