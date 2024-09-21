package chiralsoftware.slowpdfs;

import com.itextpdf.text.Document;
import static com.itextpdf.text.PageSize.LETTER;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import static com.itextpdf.text.pdf.PdfWriter.getInstance;
import java.io.ByteArrayOutputStream;
import java.io.File;
import static java.io.File.createTempFile;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import static java.nio.file.Files.copy;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.of;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;

/**
 * A Service which generates ZIP files of PDFs ... slowly, in the background
 */
@Controller
@RequiredArgsConstructor
public class GenerateService {

    private static final Logger LOG = Logger.getLogger(GenerateService.class.getName());
    
    private final SimpMessagingTemplate template;
    
    private byte[] generatePdf(int count) throws Exception {
        final Document document = new Document(LETTER, 72, 72, 72, 72); // document with one inch margins
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final PdfWriter writer = getInstance(document, os);
        document.open();
        document.add(new Paragraph("This is PDF report document #" + count + 
                ", generated at: " + new Date()));
        document.close();
        writer.close();
        return os.toByteArray();
    }
    
    private final Map<Integer,File> tempFilesMap = new HashMap<>();
    
    /** Return type void on a method with a HttpServletResponse method means that
     the method must fully handle the response, which is what we do here,
     to avoid having to load the file into memory */
    @GetMapping("/download/{number}.zip")
    public void write(@PathVariable Integer number, HttpServletResponse response) throws IOException {
        LOG.info("Need to write response number: " + number);
        if(! tempFilesMap.containsKey(number)) {
            LOG.info("That number: " + number + " was not found in the map, which contains: " + tempFilesMap.size() + " items");
            response.setStatus(SC_NOT_FOUND);
            return;
        }
        final File f = tempFilesMap.get(number);
        if(! f.canRead()) {
            LOG.info("File: " + f + " can't read");
            response.setStatus(SC_NOT_FOUND);
            return;
        }
        response.setContentLengthLong(f.length());
        response.setContentType("application/zip");
        response.setStatus(SC_OK);
        final ServletOutputStream sos = response.getOutputStream();
        final long bytesCopied = copy(f.toPath(), sos);
        LOG.info("I copied: " + bytesCopied + " bytes");
        sos.close();
    }
    
//    private static final int numberToGenerate = 100;
    private static final int sleepDuration = 100;
    
    @Async
    public void generate(int numberToGenerate) throws Exception {
        LOG.info("I got a call to generate which will run asynchronously");
        final File outputFile = createTempFile("archive-", ".zip");
        final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
        for(int i = 1; i <= numberToGenerate; i++) {
            final ZipEntry zipEntry = new ZipEntry(i + ".pdf");
            zos.putNextEntry(zipEntry);
            zos.write(generatePdf(i));
            zos.closeEntry();
            template.convertAndSend("/topic/status", 
                    of("progress", Float.toString((float) i / numberToGenerate), 
                    "message", i + ".pdf"));
            sleep(sleepDuration);
        }
        zos.close();
        LOG.info("I closed the file; it is now: " + outputFile.length() + " bytes");
        final Integer newValue;
        synchronized(tempFilesMap) {
             newValue = tempFilesMap.keySet().stream().max(comparing(Integer::valueOf)).orElse(0) + 1;
             tempFilesMap.put(newValue, outputFile);
        }
        LOG.info("I saved the temp file as: " + newValue);
        template.convertAndSend("/topic/status", 
                    of("progress", Float.toString(1f), 
                    "message", "Report complete at: " + new Date() + ", " + numberToGenerate + " files",
                    "status", "complete",
                    "filename", Integer.toString(newValue)
                    ));
    }
    
}
