package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.predicates.MiMCHash;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static zkstrata.utils.GadgetUtils.isInstanceVariable;

@AstElement(MiMCHash.class)
public class MiMCHashGadget extends AbstractGadget {
    @Type({Any.class})
    private WitnessVariable preimage;

    @Type({HexLiteral.class})
    private Variable image;

    public MiMCHashGadget() {
    }

    public MiMCHashGadget(WitnessVariable preimage, Variable image) {
        this.preimage = preimage;
        this.image = image;

        this.initialize();
    }

    /**
     * Check whether two hash predicates for different images are claimed on the same preimage.
     * <p>
     * This contradiction is only checked if both hash predicates operate on public images.
     *
     * @param hg1 first {@link MiMCHashGadget} to check
     * @param hg2 second {@link MiMCHashGadget} to check
     */
    @Contradiction
    public static void checkContradiction(MiMCHashGadget hg1, MiMCHashGadget hg2) {
        if (hg1.getPreimage().equals(hg2.getPreimage())
                && isInstanceVariable(hg1.getImage()) && isInstanceVariable(hg2.getImage())
                && !hg1.getImage().equals(hg2.getImage()))
            throw new CompileTimeException("Contradiction.", List.of(hg1.getImage(), hg2.getImage()));
    }

    @Override
    public void initialize() {
        if (isInstanceVariable(this.image)) {
            BigInteger imageValue = (BigInteger) (((HexLiteral) this.image.getValue()).getValue());
            if (imageValue.compareTo(Constants.ED25519_MAX_VALUE) > 0
                    || imageValue.compareTo(BigInteger.ZERO) < 0)
                throw new CompileTimeException(format("Invalid MiMC-Hash image. Images must be of prime order %s.",
                        Constants.ED25519_PRIME_ORDER), this.image);
        }
    }

    @Override
    public int getCostEstimate() {
        return Constants.MIMC_HASH_COST_ESTIMATE;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        MiMCHashGadget other = (MiMCHashGadget) object;
        return getPreimage().equals(other.getPreimage()) && getImage().equals(other.getImage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImage(), getPreimage());
    }

    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("preimage", preimage),
                Map.entry("image", image)
        );
        return List.of(new BulletproofsGadgetsCodeLine("HASH %(image) %(preimage)", args));
    }

    public WitnessVariable getPreimage() {
        return preimage;
    }

    public Variable getImage() {
        return image;
    }
}
