package com.abstratt.mdd.frontend.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.Step;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;

public class PackageBuilder extends DefaultParentBuilder<Package> {

    private List<NameReference> packagesImported = new ArrayList<NameReference>();
    private List<NameReference> profilesApplied = new ArrayList<NameReference>();
    private List<String> packagesLoaded = new ArrayList<String>();

    public PackageBuilder(UML2ProductKind kind) {
        super(kind);
    }

    public PackageBuilder applyProfile(String profileName) {
        this.profilesApplied.add(reference(profileName, UML2ProductKind.PROFILE));
        return this;
    }

    public PackageBuilder importPackage(String packageName) {
        this.packagesImported.add(reference(packageName, UML2ProductKind.PACKAGE));
        return this;
    }

    public ClassifierBuilder newClassifier(UML2ProductKind kind) {
        return (ClassifierBuilder) newChildBuilder(kind);
    }

    @Override
    protected Package createProduct() {
        return getContext().getRepository().createTopLevelPackage(getName(), getEClass());
    }

    @Override
    protected void enhance() {
        super.enhance();
        loadPackages();
        applyProfiles();
        importPackages();
        defineProfile();
    }

    private void loadPackages() {
        for (final String packageURI : this.packagesLoaded) {
            getContext().getReferenceTracker().add(new IDeferredReference() {
                public void resolve(IBasicRepository repository) {
                    // TODO maybe allow package loading IBasicRepository to
                    // avoid casting
                    Package loaded = ((IRepository) repository).loadPackage(URI.createURI(packageURI));
                    if (loaded == null)
                        getContext().getProblemTracker().add(
                                new UnclassifiedProblem("Could not load URI: '" + packageURI + "')"));
                }
            }, Step.PACKAGE_IMPORTS);
        }
    }

    private void importPackages() {
        for (NameReference packageName : this.packagesImported)
            new ReferenceSetter<Package>(packageName, getParentProduct(), getContext(),
                    Step.PACKAGE_IMPORTS) {
                @Override
                protected void link(Package package_) {
                    if (!getProduct().getImportedPackages().contains(package_))
                        getProduct().createPackageImport(package_, VisibilityKind.PRIVATE_LITERAL);
                }
            };
    }

    private void defineProfile() {
        if (getProduct() instanceof Profile)
            getContext().getReferenceTracker().add(new IDeferredReference() {
                @Override
                public void resolve(IBasicRepository repository) {
                    ((Profile) getProduct()).define();
                }
            }, Step.DEFINE_PROFILES);
    }

    private void applyProfiles() {
        for (NameReference profileName : this.profilesApplied)
            new ReferenceSetter<Profile>(profileName, getParentProduct(), getContext(),
                    Step.PROFILE_APPLICATIONS) {
                @Override
                protected void link(Profile profile) {
                    if (!profile.isDefined())
                        getContext().getProblemTracker().add(
                                new InternalProblem("Profile '" + profile.getName() + "' not defined"));
                    else {
                        if (!getProduct().getAppliedProfiles().contains(profile))
                            getProduct().applyProfile(profile);
                        if (!getProduct().getImportedPackages().contains(profile))
                            getProduct().createPackageImport(profile, VisibilityKind.PRIVATE_LITERAL);
                    }
                }
            };
    }

    public PackageBuilder load(String packageURI) {
        this.packagesLoaded.add(packageURI);
        return this;
    }

    public AssociationBuilder newAssociation() {
        return newChildBuilder(UML2ProductKind.ASSOCIATION);
    }
}
