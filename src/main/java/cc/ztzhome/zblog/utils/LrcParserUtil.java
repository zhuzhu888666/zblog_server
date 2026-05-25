package cc.ztzhome.zblog.utils;

import lombok.Data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LrcParserUtil {

    @Data
    public static class LyricLine {
        private long time; // 时间戳（毫秒）
        private String text; // 歌词文本

        public LyricLine(long timeMillis, String text) {
            this.time = timeMillis;
            this.text = text;
        }

        // 构造函数、getter和setter
    }

    public static List<LyricLine> parseLrc(InputStream inputStream) {
        List<LyricLine> lyrics = new ArrayList<>();
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+\\.?\\d*)\\]\\s*(.*)");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    int min = Integer.parseInt(matcher.group(1));
                    double sec = Double.parseDouble(matcher.group(2));
                    String text = matcher.group(3);

                    long timeMillis = (long) ((min * 60 + sec) * 1000);
                    lyrics.add(new LyricLine(timeMillis, text));
                }
            }
        }
        return lyrics;
    }
}