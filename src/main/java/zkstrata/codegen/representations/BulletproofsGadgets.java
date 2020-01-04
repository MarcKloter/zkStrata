package zkstrata.codegen.representations;

import zkstrata.codegen.TargetFormat;

import java.util.List;

public interface BulletproofsGadgets extends TargetFormat {
    List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets();
}
