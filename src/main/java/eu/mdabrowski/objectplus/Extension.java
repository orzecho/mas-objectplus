package eu.mdabrowski.objectplus;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.sun.istack.internal.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Extension {
    @NotNull
    @Id
    private Class klass;

    @Lob
    private byte[] extensions;
}
