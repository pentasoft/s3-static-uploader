package io.pst.mojo.s3.sta.uploader.config;

import java.util.List;

public class Include {

    /**
     * The bind of a pattern with the metadata to apply to
     * all the files matching it.
     * @parameter
     * @required
     */
    Bind bind;
    
    /**
     * List of binds specifying constraints in order to 
     * override metadata for a subset of files.
     * @parameter
     */
    List<Bind> constraints;

    public Bind getBind() {
        return bind;
    }

    public List<Bind> getContraints() {
        return constraints;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("include [")
                .append(" bind=").append(bind)
                .append(" constraints=").append(constraints)
                .append(" ]");
        return builder.toString();
    }
}
