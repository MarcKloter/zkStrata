package zkstrata.domain.data.schemas.metadata;

import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.schemas.Metadata;
import zkstrata.domain.data.types.custom.HexLiteral;

public class MerkleTree extends AbstractSchema implements Metadata {
    private HexLiteral rootHash;

    @Override
    public String getIdentifier() {
        return "merkle_tree";
    }
}
