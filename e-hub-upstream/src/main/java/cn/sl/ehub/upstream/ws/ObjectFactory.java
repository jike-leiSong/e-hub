
package cn.sl.ehub.upstream.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nari.webservice package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Cmd_QNAME = new QName("http://webservice.nari.com/", "cmd");
    private final static QName _CmdResponse_QNAME = new QName("http://webservice.nari.com/", "cmdResponse");
    private final static QName _CommitFile_QNAME = new QName("http://webservice.nari.com/", "commitFile");
    private final static QName _CommitFileResponse_QNAME = new QName("http://webservice.nari.com/", "commitFileResponse");
    private final static QName _Declare_QNAME = new QName("http://webservice.nari.com/", "declare");
    private final static QName _DeclareResponse_QNAME = new QName("http://webservice.nari.com/", "declareResponse");
    private final static QName _Exception_QNAME = new QName("http://webservice.nari.com/", "Exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nari.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Cmd }
     * 
     */
    public Cmd createCmd() {
        return new Cmd();
    }

    /**
     * Create an instance of {@link CmdResponse }
     * 
     */
    public CmdResponse createCmdResponse() {
        return new CmdResponse();
    }

    /**
     * Create an instance of {@link CommitFile }
     * 
     */
    public CommitFile createCommitFile() {
        return new CommitFile();
    }

    /**
     * Create an instance of {@link CommitFileResponse }
     * 
     */
    public CommitFileResponse createCommitFileResponse() {
        return new CommitFileResponse();
    }

    /**
     * Create an instance of {@link Declare }
     * 
     */
    public Declare createDeclare() {
        return new Declare();
    }

    /**
     * Create an instance of {@link DeclareResponse }
     *
     */
    public DeclareResponse createDeclareResponse() {
        return new DeclareResponse();
    }

    /**
     * Create an instance of {@link Exception }
     *
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Cmd }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Cmd }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "cmd")
    public JAXBElement<Cmd> createCmd(Cmd value) {
        return new JAXBElement<Cmd>(_Cmd_QNAME, Cmd.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmdResponse }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CmdResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "cmdResponse")
    public JAXBElement<CmdResponse> createCmdResponse(CmdResponse value) {
        return new JAXBElement<CmdResponse>(_CmdResponse_QNAME, CmdResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommitFile }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CommitFile }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "commitFile")
    public JAXBElement<CommitFile> createCommitFile(CommitFile value) {
        return new JAXBElement<CommitFile>(_CommitFile_QNAME, CommitFile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommitFileResponse }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CommitFileResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "commitFileResponse")
    public JAXBElement<CommitFileResponse> createCommitFileResponse(CommitFileResponse value) {
        return new JAXBElement<CommitFileResponse>(_CommitFileResponse_QNAME, CommitFileResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Declare }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Declare }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "declare")
    public JAXBElement<Declare> createDeclare(Declare value) {
        return new JAXBElement<Declare>(_Declare_QNAME, Declare.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeclareResponse }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DeclareResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "declareResponse")
    public JAXBElement<DeclareResponse> createDeclareResponse(DeclareResponse value) {
        return new JAXBElement<DeclareResponse>(_DeclareResponse_QNAME, DeclareResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}
     */
    @XmlElementDecl(namespace = "http://webservice.nari.com/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

}
