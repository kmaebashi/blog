package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSummaryDto;
import com.kmaebashi.nctfw.PlainTextResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RssService {
    private RssService() {};

    public static PlainTextResult createRss(ServiceInvoker invoker, String blogId, String contextUrl) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            List<BlogPostSummaryDto> postDtoList
                    = BlogPostDbAccess.getBlogPostRssList(context.getDbAccessInvoker(), blogId, 50);
            Document doc = createRssDocument(blogDto, postDtoList, contextUrl);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer tf = factory.newTransformer();

            tf.setOutputProperty("indent", "yes");
            tf.setOutputProperty("encoding", "UTF-8");

            CharArrayWriter writer = new CharArrayWriter();
            tf.transform(new DOMSource(doc), new StreamResult(writer));

            PlainTextResult ptr = new PlainTextResult(writer.toString(), "text/xml", null);

            return ptr;
        });
    }

    private static DateTimeFormatter rssDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static Document createRssDocument(BlogDto blogDto, List<BlogPostSummaryDto> postDtoList, String contextUrl)
            throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rdfRoot = doc.createElement("rdf:RDF");
        doc.appendChild(rdfRoot);
        rdfRoot.setAttribute("xmlns", "http://purl.org/rss/1.0/");
        rdfRoot.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        rdfRoot.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        rdfRoot.setAttribute("xml:lang", "ja");

        Element channel = addChildElement(doc, rdfRoot, "channel");
        channel.setAttribute("rdf:about", contextUrl + "/" + blogDto.blogId + "/post.rdf");
        Element title = addChildElement(doc, channel, "title");
        title.setTextContent(blogDto.title);
        Element link = addChildElement(doc, channel, "link");
        link.setTextContent(contextUrl + "/" + blogDto.blogId);
        Element description = addChildElement(doc, channel, "description");
        description.setTextContent(blogDto.description);

        Element items = addChildElement(doc, channel, "items");
        Element seq = addChildElement(doc, items, "rdf:Seq");
        for (BlogPostSummaryDto postDto : postDtoList) {
            Element li = addChildElement(doc, seq, "rdf:li");
            li.setAttribute("rdf:resource", createPostUrl(contextUrl, blogDto.blogId, postDto.blogPostId));
        }
        for (BlogPostSummaryDto postDto : postDtoList) {
            Element item = addChildElement(doc, rdfRoot, "item");
            item.setAttribute("rdf:about", createPostUrl(contextUrl, blogDto.blogId, postDto.blogPostId));
            Element title2 = addChildElement(doc, item, "title");
            title2.setTextContent(postDto.title);
            Element link2 = addChildElement(doc, item, "link");
            link2.setTextContent(createPostUrl(contextUrl, blogDto.blogId, postDto.blogPostId));
            Element description2 = addChildElement(doc,item, "description");
            description2.setTextContent(Util.cutString(postDto.sectionText, Constants.RSS_DESCRIPTION_LENGTH));
            Element date = addChildElement(doc, item, "dc:date");
            date.setTextContent(rssDateFormat.format(postDto.postedDate) + Constants.RSS_TZD);
        }

        return doc;
    }

    private static Element addChildElement(Document doc, Element parent, String childTagName)
    {
        Element child = doc.createElement(childTagName);
        parent.appendChild(child);

        return child;
    }

    private static String createPostUrl(String contextUrl, String blogId, int postId) {
        return contextUrl + "/" + blogId + "/post/" + postId;
    }
}
