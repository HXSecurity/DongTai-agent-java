package com.secnium.iast.core.replay;

import com.secnium.iast.core.handler.models.IastReplayModel;
import org.junit.Test;

public class HttpRequestReplayTest {
    @Test
    public void sendReplay() {
        HttpRequestReplay requestReplay = new HttpRequestReplay();
        String headers = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\nCookie: JSESSIONID=D61EB7BF90FB44D0AA9D3104C2D5128C\nAccept-Encoding: gzip, deflate, br\nAccept-Language: zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6\nCache-Control: max-age=0\nConnection: keep-alive\nHost: localhost:8080\nReferer: http://127.0.0.1:8080/\nSec-Fetch-Dest: document\nSec-Fetch-Mode: navigate\nSec-Fetch-Site: cross-site\nSec-Fetch-User: ?1\nUpgrade-Insecure-Requests: 1\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36\n";
        headers = "host:127.0.0.1:8080\nconnection:keep-alive\ncache-control:max-age=0\nupgrade-insecure-requests:1\nuser-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36\naccept:text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\nsec-fetch-site:none\nsec-fetch-mode:navigate\nsec-fetch-user:?1\nsec-fetch-dest:document\naccept-encoding:gzip, deflate, br\naccept-language:zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6\ncookie:csrftoken=A00l4Ok1bkiWiG1OWbPgneUiM5uFGnzVyfH4qllr1hTvw3QCmqjG0VqCnwfga8PF; _jspxcms=1b40610d1eb840498b9826c7b2809418; __utma=96992031.1435321630.1598931302.1598931302.1598931302.1; __utmz=96992031.1598931302.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lvt_bdff1c1dcce971c3d986f9be0921a0ee=1598346920,1598434566,1599821526; JSESSIONID=CAC3F2B3693CA12D8B8687336479C414\n";
        IastReplayModel model = new IastReplayModel("GET", "http://127.0.0.1:8080/overpower/read-02", "id=1", null, headers, "tomcat-docbase.8875655643773530017.8080-sql-6ca94c46-972d-4ddf-b7a7-cb2cf7d95ac8");
        HttpRequestReplay.sendReplayRequest(model);
        requestReplay.send();
    }

    @Test
    public void sendReplayFromChromCopy() {
        HttpRequestReplay requestReplay = new HttpRequestReplay();
        String headers = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\nCookie: JSESSIONID=D61EB7BF90FB44D0AA9D3104C2D5128C\nAccept-Encoding: gzip, deflate, br\nAccept-Language: zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6\nCache-Control: max-age=0\nConnection: keep-alive\nHost: localhost:8080\nReferer: http://127.0.0.1:8080/\nSec-Fetch-Dest: document\nSec-Fetch-Mode: navigate\nSec-Fetch-Site: cross-site\nSec-Fetch-User: ?1\nUpgrade-Insecure-Requests: 1\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36\n";
        headers = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\nAccept-Encoding: gzip, deflate, br\nAccept-Language: zh-CN,zh;q=0.9\nCache-Control: max-age=0\nConnection: keep-alive\nCookie: JSESSIONID=6BA6898A34FBED7BC569A4E1D11B7A55\nHost: 127.0.0.1:8080\nSec-Fetch-Dest: document\nSec-Fetch-Mode: navigate\nSec-Fetch-Site: none\nSec-Fetch-User: ?1\nUpgrade-Insecure-Requests: 1\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36";
        IastReplayModel model = new IastReplayModel("GET", "http://127.0.0.1:8080/overpower/read-02", "id=1", null, headers, "tomcat-docbase.8875655643773530017.8080-sql-6ca94c46-972d-4ddf-b7a7-cb2cf7d95ac8");
        HttpRequestReplay.sendReplayRequest(model);
        requestReplay.send();
    }
}
