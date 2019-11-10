package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.predicates.MiMCHash;
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

@AstElement(MiMCHash.class)
public class MiMCHashGadget extends AbstractGadget<MiMCHashGadget> {
    @Type({Any.class})
    private WitnessVariable preimage;

    @Type({HexLiteral.class})
    private Variable image;

    public MiMCHashGadget() {
    }

    public MiMCHashGadget(WitnessVariable preimage, Variable image) {
        this.preimage = preimage;
        this.image = image;

        this.performChecks();
    }

    /**
     * Check whether two hash predicates for different images are claimed on the same preimage.
     * <p>
     * This contradiction is only checked if both hash predicates operate on public images.
     *
     * @param hg1 first {@link MiMCHashGadget} to check
     * @param hg2 second {@link MiMCHashGadget} to check
     */
    @Contradiction(propositions = {MiMCHashGadget.class, MiMCHashGadget.class})
    public static void checkContradiction(MiMCHashGadget hg1, MiMCHashGadget hg2) {
        if (hg1.getPreimage().equals(hg2.getPreimage())
                && hg1.getImage() instanceof InstanceVariable
                && hg2.getImage() instanceof InstanceVariable
                && !hg1.getImage().equals(hg2.getImage()))
            throw new CompileTimeException("Contradiction.", Set.of(hg1.getImage(), hg2.getImage()));
    }

    public WitnessVariable getPreimage() {
        return preimage;
    }

    public Variable getImage() {
        return image;
    }

    @Override
    public void performChecks() {
        if (this.image instanceof InstanceVariable) {
            BigInteger image = (BigInteger) (((HexLiteral) this.image.getValue()).getValue());
            if (image.compareTo(SemanticsUtils.ED25519_MAX_VALUE) > 0
                    || image.compareTo(BigInteger.ZERO) < 0)
                throw new CompileTimeException(String.format("Invalid MiMC hash image. Images must be of prime order %s.",
                        SemanticsUtils.ED25519_PRIME_ORDER), this.image);
        }
    }

    @Override
    public boolean isEqualTo(MiMCHashGadget other) {
        return preimage.equals(other.preimage) && image.equals(other.image);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("preimage", preimage),
                Map.entry("image", image)
        );
        return new TargetFormat("HASH %(image) %(preimage)", args);
    }
}
